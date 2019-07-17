package com.ddf.scaffold.fw.keepalive.server;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ddf.scaffold.fw.util.SpringContextHolder;
import com.ddf.scaffold.logic.entity.ChannelInfo;
import com.ddf.scaffold.logic.mapper.ChannelInfoMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 连接信息同步任务类
 *
 * @author dongfang.ding
 * @date 2019/7/8 10:12
 */
@Slf4j
public class ChannelStoreSyncTask implements Runnable {

    private ExecutorService executorService;

    private ChannelInfoMapper channelInfoMapper = SpringContextHolder.getBean(ChannelInfoMapper.class);

    private Map<String, ChannelMonitor> noDeviceIdList = new ConcurrentHashMap<>();

    public ChannelStoreSyncTask(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ChannelStoreSyncTask() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * 这一块如果是与数据库同步的话，下面处理没有这么复杂，就简单很多了,连接状态和消息需要分开处理
     * 该方法必须要有先后顺序，不能后面的状态先处理再处理前面的数据状态，可能会导致覆盖
     */
    @Override
    public synchronized void run() {
        log.info("===========================同步连接信息========================");
        log.info("排错用： 未同步到设备id: [{}]", noDeviceIdList);
        final ConcurrentHashMap<String, ChannelMonitor> channels = ServerInboundHandler.channelStore;
        if (!channels.isEmpty()) {
            for (Map.Entry<String, ChannelMonitor> entry : channels.entrySet()) {
                executorService.execute(() -> {
                    try {
                        String key = entry.getKey();
                        ChannelMonitor channelMonitor = entry.getValue();
                        if (!channelMonitor.isSyncDone()) {
                            ChannelInfo channelInfo = ChannelInfo.build(channelMonitor);
                            LambdaQueryWrapper<ChannelInfo> queryWrapper = Wrappers.lambdaQuery();
                            queryWrapper.eq(ChannelInfo::getRemoteAddress, key);
                            ChannelInfo persistence = channelInfoMapper.selectOne(queryWrapper);
                            if (channelInfo.getDeviceId() == null) {
                                noDeviceIdList.put(key, channelMonitor);
                                return;
                            }
                            noDeviceIdList.remove(key);
                            if (persistence == null) {
                                channelInfoMapper.insert(channelInfo);
                            } else {
                                persistence.setStatus(channelInfo.getStatus());
                                persistence.setChangeTime(new Date());
                                persistence.setDeviceId(channelInfo.getDeviceId());
                                LambdaUpdateWrapper<ChannelInfo> updateWrapper = Wrappers.lambdaUpdate();
                                updateWrapper.eq(ChannelInfo::getId, persistence.getId());
                                channelInfoMapper.update(persistence, updateWrapper);
                            }
                            channelMonitor.setSyncDone(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
