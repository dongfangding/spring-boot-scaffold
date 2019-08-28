package com.ddf.scaffold.fw.tcp.client;

import com.ddf.scaffold.fw.tcp.server.DeviceRegistry;
import com.ddf.scaffold.fw.tcp.server.KeyManagerFactoryHelper;
import com.ddf.scaffold.fw.tcp.server.RequestContent;
import com.ddf.scaffold.fw.tcp.server.SmsContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dongfang.ding
 * @date 2019/7/5 11:12
 */
@Slf4j
public class TCPClient {

    private String host;
    private int port;
    private volatile Channel channel;
    private Executor executorService;
    private NioEventLoopGroup worker;
    private boolean startSsl;

    public TCPClient(String host, int port, Executor executorService, boolean startSsl) {
        this.host = host;
        this.port = port;
        this.executorService = executorService;
        this.startSsl = startSsl;
    }

    public void connect() {
        executorService.execute(() -> {
            worker = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            bootstrap.remoteAddress(host, port);
            try {
                if (startSsl) {
                    bootstrap.handler(new ClientChannelInit(KeyManagerFactoryHelper.defaultClientContext()));
                } else {
                    bootstrap.handler(new ClientChannelInit());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ChannelFuture future;
            try {
                future = bootstrap.connect().sync();
                if (future.isSuccess()) {
                    log.info("连接到服务端端成功....");
                }
                channel = future.channel();
                log.info("客户端初始化完成............");
                // 这里会一直与服务端保持连接，直到服务端断掉才会同步关闭自己,所以是阻塞状态，如果不实用线程的话，无法将对象暴露出去给外部调用
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized void write(RequestContent content) {
        while (channel == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        channel.writeAndFlush((content));
    }

    public void close() {
        try {
            log.info("客户端尝试主动close..............");
            channel.close();
        } finally {
            try {
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 客户端只是粗略实验代码
     *
     * @param args
     * @throws JsonProcessingException
     * @throws InterruptedException
     */
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 1; i++) {
            executorService.execute(() -> {
//                registryDevice(executorService);
                sendMsg(executorService);
            });
        }
    }

    private static void registryDevice(ExecutorService executorService) {
        TCPClient client = new TCPClient("localhost", 9000, executorService, true);
        client.connect();
        Random random = new Random();
        String deviceNo = "deviceNo123";
        DeviceRegistry deviceRegistry = new DeviceRegistry();
        deviceRegistry.setDeviceNo(deviceNo);
        deviceRegistry.setDeviceModel("华为");
        deviceRegistry.setDeviceName("华为");
        deviceRegistry.setDeviceVersion("安卓");
        RequestContent requestContent = RequestContent.clientDeviceRegistry(deviceRegistry);
        requestContent.setDeviceNo(deviceNo);
        client.write(requestContent);
    }

    private static void sendMsg(ExecutorService executorService) {
        TCPClient client = new TCPClient("localhost", 9001, executorService, true);
        client.connect();
        Random random = new Random();
        List<SmsContent> smsList = new ArrayList<>();
        for (int j = 0; j < 1; j++) {
            try {
                // 随机睡眠
                Thread.sleep(random.nextInt(2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SmsContent smsContent = new SmsContent();
            smsContent.setMessageId(UUID.randomUUID().toString());
            smsContent.setSender("95577");
            smsContent.setReceiver("13185679963");
            smsContent.setReceiverTime(System.currentTimeMillis());
            smsContent.setContent("您的账户*0936于07月29日09:54收入人民币0.01元，余额0.86元。银联入账，付款方魏世兴。【华夏银行】");
            smsList.add(smsContent);
        }
        RequestContent request = RequestContent.clientSmsUpload(smsList);
        // 以append的方式增加扩展字段
        request.addExtra("lang", "java");
        request.setDeviceNo("华为-" + UUID.randomUUID().toString());
        // 写json串
        client.write(request);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
