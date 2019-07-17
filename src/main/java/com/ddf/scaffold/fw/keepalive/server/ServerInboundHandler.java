package com.ddf.scaffold.fw.keepalive.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dongfang.ding
 * @date 2019/7/5 15:52
 */
@Slf4j
public class ServerInboundHandler extends SimpleChannelInboundHandler<RequestContent> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static ConcurrentHashMap<String, ChannelMonitor> channelStore = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
        channelStore.put(ctx.channel().remoteAddress().toString(), ChannelMonitor.registry(ctx.channel()));
        log.info("客户端[{}]注册成功.........", ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("客户端[{}]在线.........", ctx.channel().remoteAddress());
        ChannelMonitor.active(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端[{}]掉线", ctx.channel().remoteAddress());
        ChannelMonitor.inactive(ctx.channel());
        channels.remove(ctx.channel());
    }


    /**
     * 如果是聊天室的功能，其实就是服务端收到消息之后，然后再由服务端向所有连接的客户端转发这个消息而已
     * @param ctx
     * @param msg
     * @throws JsonProcessingException
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestContent msg) throws JsonProcessingException {
        putMessage(ctx.channel(), msg);
        log.info("接收到客户端[{}]发送的数据: {}", ctx.channel().remoteAddress(), RequestContent.serial(msg));
        ctx.writeAndFlush(RequestContent.responseAccept(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        log.info("客户端[{}]出现异常，关闭连接", ctx.channel());
        ctx.close();
    }

    /**
     * 将消息放入对应的客户端的消息队列中
     *
     * @param channel
     * @param requestContent
     */
    private void putMessage(Channel channel, RequestContent requestContent) {
        synchronized (channel.toString().intern()) {
            String key = channel.remoteAddress().toString();
            ChannelMonitor channelMonitor = ServerInboundHandler.channelStore.get(key);
            // 可能永远也不会出现这种情况
            if (channelMonitor == null) {
                channelMonitor = ChannelMonitor.active(channel);
                channelMonitor.getQueue().add(requestContent);
            }
            channelMonitor.getQueue().add(requestContent);
            ChannelMonitor.bindDevice(channelMonitor, requestContent);
            ServerInboundHandler.channelStore.put(key, channelMonitor);
        }
    }
}
