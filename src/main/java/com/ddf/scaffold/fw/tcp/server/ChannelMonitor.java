package com.ddf.scaffold.fw.tcp.server;

import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;
import io.netty.channel.Channel;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 通道连接信息对象
 *
 * @author dongfang.ding
 * @date 2019/7/8 9:49
 */
@Data
public class ChannelMonitor {
    /**
     * 状态
     */
    public static final int STATUS_REGISTRY = 1;
    public static final int STATUS_ACTIVE = 2;
    public static final int STATUS_INACTIVE = 3;

    /**
     * 设备id
     */
    private String deviceNo;

    /**
     * 通道连接信息
     */
    private Channel channel;
    /**
     * 客户端远程地址
     */
    private String remoteAddress;
    /**
     * 连接状态 1 注册  2 在线 3 掉线
     */
    private int status;
    /**
     * 注册时间
     */
    private Date registryTime;
    /**
     * 最后更改时间
     */
    private Date modifyTime;
    /**
     * 每个连接信息用来存储信息接收的队列
     */
    private BlockingQueue<ChannelTransfer> queue;

    /**
     * 是否已经同步的标识(只同步连接状态，因为连接状态牵扯到状态更新，传输内容是使用队列的，
     * 到时候只要判断队列中是否有数据就可以处理)，防止为了没处理过的数据去浪费时间
     */
    private boolean syncDone;

    public ChannelMonitor() {

    }

    public ChannelMonitor(Channel channel, String remoteAddress, int status, Date registryTime, Date modifyTime,
                          BlockingQueue<ChannelTransfer> queue, boolean syncDone) {
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.status = status;
        this.registryTime = registryTime;
        this.modifyTime = modifyTime;
        this.queue = queue;
        this.syncDone = syncDone;
    }

    /**
     * 注册时连接信息包装
     *
     * @param channel
     * @return
     */
    public static ChannelMonitor registry(Channel channel) {
        return new ChannelMonitor(channel, channel.remoteAddress().toString(), STATUS_REGISTRY, new Date(), new Date(),
                new ArrayBlockingQueue<>(1024), false);
    }

    /**
     * 连接激活时修改连接信息,但是此时没有设备Id
     *
     * @param channel
     * @return
     */
    public static ChannelMonitor active(Channel channel) {
        return modify(channel, STATUS_ACTIVE);
    }


    /**
     * 将已经存在的连接待收到客户端数据后与设备id绑定在一起
     * @param channelMonitor
     * @param requestContent
     * @return
     */
    public static ChannelMonitor bindDevice(ChannelMonitor channelMonitor, RequestContent requestContent) {
        // 设备id只会绑定到一个连接上一次，如果重连肯定是一个新的连接
        if (requestContent != null && channelMonitor != null && StringUtils.isEmpty(channelMonitor.getDeviceNo())) {
            channelMonitor.setStatus(STATUS_ACTIVE);
            channelMonitor.setModifyTime(new Date());
            channelMonitor.setSyncDone(false);
            channelMonitor.setDeviceNo(requestContent.getDeviceNo());
        }
        return channelMonitor;
    }

    /**
     * 连接掉线时修改连接信息
     *
     * @param channel
     * @return
     */
    public static ChannelMonitor inactive(Channel channel) {
        return modify(channel, STATUS_INACTIVE);
    }

    private static ChannelMonitor modify(Channel channel, int status) {
        String key = channel.remoteAddress().toString();
        ChannelMonitor info = ChannelTransferStore.get(key);
        if (info == null) {
            info = registry(channel);
        }
        info.setStatus(status);
        info.setModifyTime(new Date());
        info.setSyncDone(false);
        return info;
    }

    public static String status2String(int status) {
        if (status == STATUS_REGISTRY) {
            return "注册";
        } else if (status == STATUS_ACTIVE) {
            return "激活";
        } else if (status == STATUS_INACTIVE) {
            return "掉线";
        }
        return null;
    }
}
