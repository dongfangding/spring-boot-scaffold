package com.ddf.scaffold.fw.tcp.server;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务配置类
 *
 * @author dongfang.ding
 * @date 2019/7/16 10:29
 */
@Component
@ConfigurationProperties(prefix = "custom.tcp")
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
     * 同步数据库间隔时间.单位秒
     */
    private int syncDbIntervalSeconds;

    /**
     * 服务端启动成功后回调接口bean的名称
     * @see ServerStartListener
     */
    private String serverStartListener;

    /**
     * 重试解析客户端传送给服务端的数据报文日志的定时cron表达式
     */
    private String retryCron;

    /**
     * 长连接服务集群使用，其它服务端的地址；
     * 针对该项目，主要是为了及时感知其它下线的服务器，然后由其它存活机器来释放下线服务器的数据
     */
    private List<ServerAddress> serverAddressList = new ArrayList<>();

    /**
     * 其它长连接服务配置类
     */
    @Getter
    @Setter
    public class ServerAddress {
        private String address;
        private int port;
    }
}
