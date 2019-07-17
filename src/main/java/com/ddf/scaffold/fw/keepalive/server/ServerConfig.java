package com.ddf.scaffold.fw.keepalive.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置类
 *
 * @author dongfang.ding
 * @date 2019/7/16 10:29
 */
@Configuration
@ConfigurationProperties(prefix = "customs.keepalive")
@Data
public class ServerConfig {

    /**
     * 服务端端口
     */
    private int port;

    /**
     * 是否开启ssl
     */
    private boolean startSsl;

    /**
     * 心跳检测间隔时间,单位秒
     */
    private int heartIntervalSeconds;

    /**
     * 是否开启与数据库同步功能
     */
    private boolean startSync = true;

    /**
     * 同步间隔时间.单位秒
     */
    private int syncIntervalSeconds;
}
