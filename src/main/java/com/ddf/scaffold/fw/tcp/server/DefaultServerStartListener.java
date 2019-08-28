package com.ddf.scaffold.fw.tcp.server;

import io.netty.channel.ChannelFuture;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 服务端启动成功后的回调接口实现类
 *
 * @author dongfang.ding
 * @date 2019/8/8 10:25
 */
@Component
public class DefaultServerStartListener implements ServerStartListener {


    /**
     * 启动定时任务去管理同步客户端传送的数据
     *
     * @param serverConfig 服务端配置类
     * @param future       服务端连接
     */
    @Override
    public void doAction(ServerConfig serverConfig, ChannelFuture future) {
        if (serverConfig.isStartSync()) {
            int syncInterval = serverConfig.getSyncDbIntervalSeconds() == 0 ? 60 : serverConfig.getSyncDbIntervalSeconds();
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ChannelStoreSyncTask(), syncInterval, syncInterval, TimeUnit.SECONDS);
        }
    }
}
