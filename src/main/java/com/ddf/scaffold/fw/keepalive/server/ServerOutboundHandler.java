package com.ddf.scaffold.fw.keepalive.server;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongfang.ding
 * @date 2019/7/5 15:16
 */
@Slf4j
public class ServerOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws JsonProcessingException {
        log.info("向客户端[{}]发送数据: {}", ctx.channel().remoteAddress(), RequestContent.serial((RequestContent) msg));
        ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
