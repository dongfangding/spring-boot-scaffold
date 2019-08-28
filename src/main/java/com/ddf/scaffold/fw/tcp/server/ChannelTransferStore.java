package com.ddf.scaffold.fw.tcp.server;

import com.ddf.scaffold.fw.tcp.model.datao.BankSms;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接传输临时存储库
 *
 * <pre class="code">
 * 流程
 * 1. 服务端监听到客户端的连接，使用客户端的地址(ip+端口)作为key，存储连接专属的对象信息,包含连接状态，
 *      连接专属队列(接收客户端信息用)，设备号等{@link ChannelMonitor}.流程见{@link ServerInboundHandler#channelRegistered}
 *
 * 2. 客户端与服务端之间的纽带是通过{@link Channel}来联系的，因此上述步骤无法使用设备号作为key,连接建立之后，客户端需要传送
 *    客户端的设备号，数据报文格式见{@link RequestContent},数据传输采取加密加签的方式。服务端收到信息之后，需要解密验签
 *    判断数据有效性之后（如果无效则关闭对方连接）将报文封装成日志对象{@link ChannelTransfer}, 并标记为待处
 *    理状态{@link ChannelTransfer#setStatusVerified}。之后找到发送报文的客户端对应的{@link ChannelMonitor}对象，
 *    然后绑定设备号{@link ChannelTransfer#setDeviceNo}。同时将{@link RequestContent}报文数据放到专属的消息队列
 *    中{@link ChannelMonitor#getQueue()}。完整流程见{@link RequestContentCodec#decode}
 *
 * 3. 连接状态如果发生改变，会根据客户端地址去找到对应的{@link ChannelMonitor}然后修改连接状态，提供两个上线下线
 *      改变状态的方法{@link ChannelMonitor#active}和{@link ChannelMonitor#inactive}，完整流程见
 *      {@link ServerInboundHandler#channelActive}和{@link ServerInboundHandler#channelInactive}。
 *
 * 4. 线程池去获取存储库数据{@link ChannelTransferStore#getStore},循环处理每个连接的数据和设备状态。每当设备状态发生变化，
 *    状态都会同步到数据库中，同时也会取连接通道队列中存储的数据{@link ChannelTransfer}。每个连接的任务都是异步的，
 *    使用同一个线程池去执行。{@code ChannelTransfer}对象对应的会首先持久化到数据库中的连接传输日志表中。一旦持久化完成，
 *    则会响应给客户端状态，这个数据我已经持久化下来了，不需要再重试发送了。然后会取解析{@link ChannelTransfer#getRequestContent()}
 *    报文数据，然后将报文中的短信内容，持久化到{@link BankSms}中。如果持久化失败，本地会有其他线程去
 *    重试持久化到表中的{@link ChannelTransfer#waitToDeal}该状态的数据去解析报文数据然后重试保存。完整流程见
 *    {@link ChannelStoreSyncTask#run()}
 *
 * 5. {@link ChannelTransfer}中的连接信息如果为掉线并且队列中的数据已经被处理完，那么定时任务扫描到这个状态之后会将该连接的key删除掉，
 *     避免无效key过多占用大小。形成这种问题的原因是由于连接掉线重现后，端口号可能会发生改变，那么就会形成一个全新的
 *     客户端地址，以前的key就不会再被访问到了。如果不进行管理的话，会无限膨胀。
 * </pre>
 *
 * @author dongfang.ding
 * @date 2019/7/23 11:07
 */
public class ChannelTransferStore {

    private static ConcurrentHashMap<String, ChannelMonitor> channelStore = new ConcurrentHashMap<>(1000);

    /**
     * 返回存储连接信息（包含连接对象、通道对象、传输队列）的map
     *
     * @return
     */
    public static ConcurrentHashMap<String, ChannelMonitor> getStore() {
        return channelStore;
    }

    /**
     * 添加连接监控信息
     *
     * @param key
     * @param channelMonitor
     */
    public static void put(String key, ChannelMonitor channelMonitor) {
        channelStore.put(key, channelMonitor);
    }


    /**
     * 获取存入的监控信息
     *
     * @param key
     * @return
     */
    public static ChannelMonitor get(String key) {
        return channelStore.get(key);
    }


    /**
     * 将消息放入对应的客户端的消息队列中
     *
     * @param channel
     * @param channelTransfer
     */
    public static void putMessage(Channel channel, ChannelTransfer channelTransfer) {
        synchronized (channel.toString().intern()) {
            String key = channel.remoteAddress().toString();
            ChannelMonitor channelMonitor = channelStore.get(key);
            // 可能永远也不会出现这种情况
            if (channelMonitor == null) {
                channelMonitor = ChannelMonitor.active(channel);
            }
            channelMonitor.getQueue().add(channelTransfer);
            ChannelMonitor.bindDevice(channelMonitor, channelTransfer.getRequestContent());
            channelStore.put(key, channelMonitor);
        }
    }
}
