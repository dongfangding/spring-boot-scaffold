package com.ddf.scaffold.fw.keepalive.client;

import com.ddf.scaffold.fw.keepalive.server.RequestContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongfang.ding
 * @date 2019/7/5 17:38
 */
@Slf4j
public class ClientOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        try {
            log.info("[{}]向服务端发送数据: {}", ctx.channel().remoteAddress(), RequestContent.serial((RequestContent) msg));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ctx.writeAndFlush((msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
