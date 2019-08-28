package com.ddf.scaffold.fw.tcp.server;

import io.netty.channel.*;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

/**
 *
 * 服务端的handler
 *
 * @author dongfang.ding
 * @date 2019/7/5 10:49
 */
@Slf4j
public class ServerChannelInit extends ChannelInitializer<Channel> {

    private final SslContext context;
    private ServerConfig serverConfig;

    public ServerChannelInit(ServerConfig serverConfig) {
        context = null;
        this.serverConfig = serverConfig;
    }

    public ServerChannelInit(SslContext context, ServerConfig serverConfig) {
        this.context = context;
        this.serverConfig = serverConfig;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (pipeline != null) {
            if (context != null) {
                SSLEngine engine = context.newEngine(ch.alloc());
                engine.setUseClientMode(false);
                ch.pipeline().addFirst("ssl", new SslHandler(engine));
            }

            // 添加换行符解码器，以及自定义编解码器,客户端每次传输数据必须以"\r\n"结尾并且符合自定义解码器规则
            pipeline.addLast(new LineBasedFrameDecoder(102400)).addLast(new RequestContentCodec(true))
                    .addLast(new ServerOutboundHandler()).addLast(new ServerInboundHandler())
                    // IdleStateHandler 将通过 IdleStateEvent 调用 userEventTriggered ，如果连接没有接收或发送数据超过90秒钟
                    .addLast(new IdleStateHandler(0, 0,
                            serverConfig.getHeartIntervalSeconds() == 0 ? 90 : serverConfig.getHeartIntervalSeconds(), TimeUnit.SECONDS))
                    .addLast(new HeartbeatHandler());
        }
    }


    /**
     * 心跳检测类
     */
    public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                if (ctx.channel().isActive()) {
                    // 发送的心跳并添加一个侦听器，如果发送操作失败将关闭连接
                    try {
                        ctx.writeAndFlush(RequestContent.serverHeart())
                                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    } catch (Exception e) {
                        log.info("向客户端[{}]发送心跳包失败", ctx.channel());
                    }
                }
            } else {
                // 事件不是一个 IdleStateEvent 的话，就将它传递给下一个处理程序
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
