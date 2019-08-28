package com.ddf.scaffold.fw.listeners;

import com.ddf.scaffold.fw.tcp.server.ServerConfig;
import com.ddf.scaffold.fw.tcp.server.TCPServer;
import com.ddf.scaffold.fw.tcp.service.ChannelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * 容器启动时间
 *
 * @author dongfang.ding
 * @date 2019/7/16 10:38
 */
@Component
public class ContextStartListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private ChannelInfoService channelInfoService;
    private AtomicBoolean init = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!init.get()) {
            // 启动tcp长连接服务
            taskExecutor.execute(() -> {
                init.set(true);
                channelInfoService.invalidConnection();
                new TCPServer(serverConfig).start();
            });
        }
    }
}
