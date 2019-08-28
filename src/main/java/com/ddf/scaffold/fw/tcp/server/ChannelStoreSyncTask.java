package com.ddf.scaffold.fw.tcp.server;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ddf.scaffold.fw.tcp.biz.ChannelTransferBizService;
import com.ddf.scaffold.fw.tcp.mapper.ChannelInfoMapper;
import com.ddf.scaffold.fw.tcp.mapper.ChannelTransferMapper;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelInfo;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;
import com.ddf.scaffold.fw.tcp.service.ChannelTransferService;
import com.ddf.scaffold.fw.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 连接信息同步任务类
 *
 * @author dongfang.ding
 * @date 2019/7/8 10:12
 */
@Slf4j
public class ChannelStoreSyncTask implements Runnable {

    private Executor executorService;

    private ChannelInfoMapper channelInfoMapper = SpringContextHolder.getBean(ChannelInfoMapper.class);

    private ChannelTransferMapper channelTransferMapper = SpringContextHolder.getBean(ChannelTransferMapper.class);

    private ChannelTransferService channelTransferService = SpringContextHolder.getBean(ChannelTransferService.class);

    private ChannelTransferBizService channelTransferBizService = SpringContextHolder.getBean(ChannelTransferBizService.class);

    private Map<String, ChannelMonitor> noDeviceIdList = new ConcurrentHashMap<>();

    public ChannelStoreSyncTask(Executor executorService) {
        this.executorService = executorService;
    }

    public ChannelStoreSyncTask() {
        this.executorService = (ThreadPoolTaskExecutor) SpringContextHolder.getBean("transferQueueExecutor");
    }

    /**
     * 这一块如果是与数据库同步的话，下面处理没有这么复杂，就简单很多了,连接状态和消息需要分开处理
     * 该方法必须要有先后顺序，不能后面的状态先处理再处理前面的数据状态，可能会导致覆盖
     */
    @Override
    public synchronized void run() {
        log.info("===========================同步连接信息========================");
        log.info("排错用： 未同步到设备id: [{}]", noDeviceIdList);
        log.info("当前连接大小: " + ChannelTransferStore.getStore().size());
        // 每次只处理当前数据
        final Map<String, ChannelMonitor> channels = new HashMap<>(ChannelTransferStore.getStore());
        if (!channels.isEmpty()) {
            for (Map.Entry<String, ChannelMonitor> entry : channels.entrySet()) {
                // TODO 确认
                executorService.execute(() -> {
                    try {
                        String key = entry.getKey();
                        ChannelMonitor channelMonitor = entry.getValue();
                        if (!channelMonitor.isSyncDone()) {
                            ChannelInfo channelInfo = ChannelInfo.build(channelMonitor);
                            if (channelInfo.getDeviceNo() == null) {
                                noDeviceIdList.put(key, channelMonitor);
                                return;
                            }
                            LambdaQueryWrapper<ChannelInfo> queryWrapper = Wrappers.lambdaQuery();
                            queryWrapper.eq(ChannelInfo::getClientAddress, key);
                            ChannelInfo persistence = channelInfoMapper.selectOne(queryWrapper);
                            noDeviceIdList.remove(key);
                            if (persistence == null) {
                                channelInfoMapper.insert(channelInfo);
                            } else {
                                persistence.setStatus(channelInfo.getStatus());
                                persistence.setChangeTime(new Date());
                                persistence.setDeviceNo(channelInfo.getDeviceNo());
                                LambdaUpdateWrapper<ChannelInfo> updateWrapper = Wrappers.lambdaUpdate();
                                updateWrapper.eq(ChannelInfo::getId, persistence.getId());
                                channelInfoMapper.update(persistence, updateWrapper);
                            }
                            channelMonitor.setSyncDone(true);
                        }
                        // 如果连接掉线并且队列里的数据已经同步完，则需要移除掉节省key的大小
                        if (channelMonitor.getQueue().peek() == null && ChannelMonitor.STATUS_INACTIVE == channelMonitor.getStatus()) {
                            ChannelTransferStore.getStore().remove(channelMonitor.getRemoteAddress());
                        }
                        // 持久化接收队列里的数据
                        consumerRequestContentQueue(channelMonitor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }


    /**
     * 处理接收消息队列中的数据
     *
     * @param channelMonitor
     */
    @SuppressWarnings("unchecked")
    private void consumerRequestContentQueue(ChannelMonitor channelMonitor) {
        if (channelMonitor.getQueue().peek() != null) {
            ChannelTransfer channelTransfer = channelMonitor.getQueue().poll();
            if (channelTransfer == null) {
                return;
            }
            RequestContent requestContent = channelTransfer.getRequestContent();
            log.info("RequestContext: {}", requestContent);
            if (requestContent == null) {
                return;
            }
            // 相同的requestId不允许处理多次
            if (channelTransferService.existByRequestId(requestContent.getRequestId())) {
                // 重发的话有可能是连接中断后没有收到服务器的响应，在这里做补偿,设备注册只有真的将设备保存下来才响应
                if (!RequestContent.Cmd.DEVICE_REGISTRY.name().equals(requestContent.getCmd())) {
                    // 设备注册只有当消费成功后才响应注册成功
                    ChannelTransferStore.get(channelTransfer.getClientAddress()).getChannel()
                            .writeAndFlush(RequestContent.responseOK(channelTransfer.getRequestContent()));
                }
                return;
            }
            // 记录传输日志
            channelTransferMapper.insert(channelTransfer);
            // FIXME
            // 如果日志已保存则告诉客户端，不需要重发了，我已经保存了。后面及时处理失败，服务端自己会去表里重试
            // 但是这个通知，其实是个随缘通知；如果当时发送这条消息的客户端断掉了，重连之后换了地址，其实是通知不回去的;
            // 解决方法：
            // 1. 存储连接的key使用客户端的ip即可，不需要端口；那么这样重连也能找到，但是一台设备就不能启动多个连接
            // 2. 提供一个接口，接收客户端传送的requestId，然后服务端去查询表中是否存在，如果存在返回给客户端；这样客户端就可以删除旧数据了
            // 3. 维护所有在线的设备号对应的有效Channel对象，如果失效的话就是用旧的连接对应的设备号，在根据设备号找最新的连接
            if (!RequestContent.Cmd.DEVICE_REGISTRY.name().equals(requestContent.getCmd())) {
                // 设备注册只有当消费成功后才响应注册成功
                ChannelTransferStore.get(channelTransfer.getClientAddress()).getChannel()
                        .writeAndFlush(RequestContent.responseOK(channelTransfer.getRequestContent()));
            }
            // 消费报文记录和解析短信内容
            channelTransferBizService.consumerRequestContentQueue(channelTransfer);
        }
    }
}
