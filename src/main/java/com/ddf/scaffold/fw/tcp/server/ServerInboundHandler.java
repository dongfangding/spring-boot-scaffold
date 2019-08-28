package com.ddf.scaffold.fw.tcp.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongfang.ding
 * @date 2019/7/5 15:52
 */
@Slf4j
public class ServerInboundHandler extends SimpleChannelInboundHandler<RequestContent> {

    /** 存储连接对象 */
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    public static ChannelGroup getChannels() {
        return channels;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
        ChannelTransferStore.put(ctx.channel().remoteAddress().toString(), ChannelMonitor.registry(ctx.channel()));
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
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestContent msg) {
        log.info("接收到客户端[{}]发送的数据: {}", ctx.channel().remoteAddress(), RequestContent.serial(msg));
        ctx.writeAndFlush(RequestContent.responseAccept(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        log.info("客户端[{}]出现异常，关闭连接", ctx.channel());
        ctx.close();
    }
}
