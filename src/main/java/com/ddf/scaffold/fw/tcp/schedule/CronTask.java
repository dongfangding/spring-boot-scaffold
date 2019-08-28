package com.ddf.scaffold.fw.tcp.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ddf.scaffold.fw.tcp.biz.ChannelTransferBizService;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;
import com.ddf.scaffold.fw.tcp.service.ChannelTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务类
 *
 * @author dongfang.ding
 * @date 2019/8/8 15:33
 */
@Component
@Slf4j
public class CronTask {

    @Autowired
    private ChannelTransferService channelTransferService;
    @Autowired
    private ThreadPoolTaskExecutor transferRetryExecutor;
    @Autowired
    private ChannelTransferBizService channelTransferBizService;

    /**
     * 对通道传输已保存但处理失败的数据进行重新处理
     * 注意，这个方法不要有事务，需要线程任务的每个处理有自己独立的事务
     */
    @Scheduled(cron = "#{serverConfig.retryCron}")
    public void retryChannelTransfer() {
        log.info("开始重试处理通道传输数据====================");
        LambdaQueryWrapper<ChannelTransfer> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChannelTransfer::getStatus, ChannelTransfer.waitToDeal());
        List<ChannelTransfer> list = channelTransferService.list(queryWrapper);
        log.info("扫描到需要处理的任务数: {}", list.size());
        if (!list.isEmpty()) {
            for (ChannelTransfer channelTransfer : list) {
                transferRetryExecutor.execute(() -> channelTransferBizService.consumerRequestContentQueue(channelTransfer));
            }
        }
    }
}
