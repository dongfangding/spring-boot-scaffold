package com.ddf.scaffold.fw.tcp.client;

import com.ddf.scaffold.fw.tcp.server.RequestContent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongfang.ding
 * @date 2019/7/5 11:03
 */
@Slf4j
public class ClientInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("连接到服务器成功.........");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("与服务器连接断开");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("读取到服务器的发送信息: {}", RequestContent.serial((RequestContent) msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
