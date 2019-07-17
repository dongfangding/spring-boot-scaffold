package com.ddf.scaffold.fw.listeners;

import com.ddf.scaffold.fw.keepalive.server.ServerConfig;
import com.ddf.scaffold.fw.keepalive.server.TCPServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        taskExecutor.execute(() -> new TCPServer(serverConfig).start());
    }
}
