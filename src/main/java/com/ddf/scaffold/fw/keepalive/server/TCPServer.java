package com.ddf.scaffold.fw.keepalive.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 长连接服务端
 *
 * @author dongfang.ding
 * @date 2019/7/5 10:19
 */
@Slf4j
public class TCPServer {

    private static int WORKER_GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerConfig serverConfig;

    public TCPServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


    /**
     * 启动服务端
     */
    public void start() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup(WORKER_GROUP_SIZE);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        log.info("workerGroup size: [{}]", WORKER_GROUP_SIZE);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false))
                .childOption(ChannelOption.SO_RCVBUF, 1048576)
                .childOption(ChannelOption.SO_SNDBUF, 1048576);
        try {
            if (serverConfig.isStartSsl()) {
                serverBootstrap.childHandler(new ServerChannelInit(KeyManagerFactoryHelper.defaultServerContext(), serverConfig));
            } else {
                serverBootstrap.childHandler(new ServerChannelInit(serverConfig));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChannelFuture future;
        try {
            log.info("服务端启动中.....");
            future = serverBootstrap.bind(serverConfig.getPort()).sync();
            if (future.isSuccess()) {
                log.info("服务端启动成功....");
            }
            if (serverConfig.isStartSync()) {
                int syncInterval = serverConfig.getSyncIntervalSeconds() == 0 ? 60 : serverConfig.getSyncIntervalSeconds();
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ChannelStoreSyncTask(), syncInterval, syncInterval, TimeUnit.SECONDS);
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭服务端
     */
    public void close() {
        try {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
