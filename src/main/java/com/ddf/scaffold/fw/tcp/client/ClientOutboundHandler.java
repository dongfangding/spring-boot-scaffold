package com.ddf.scaffold.fw.tcp.client;

import com.ddf.scaffold.fw.tcp.server.RequestContent;
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
        log.info("[{}]向服务端发送数据: {}", ctx.channel().remoteAddress(), RequestContent.serial((RequestContent) msg));
        ctx.writeAndFlush((msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
