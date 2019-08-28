package com.ddf.scaffold.fw.tcp.server;

import io.netty.channel.ChannelFuture;

/**
 * 服务端监听接口
 *
 * @author dongfang.ding
 * @date 2019/8/8 10:20
 */
public interface ServerStartListener {

    /**
     * 服务端启动成功后回调
     *
     * @param serverConfig 服务端配置类
     * @param future 服务端连接
     * @see ChannelFuture
     */
    void doAction(ServerConfig serverConfig, ChannelFuture future);
}
