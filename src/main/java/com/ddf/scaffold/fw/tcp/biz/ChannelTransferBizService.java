package com.ddf.scaffold.fw.tcp.biz;


import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;

/**
 * 通道传输日志处理业务层
 *
 * @author dongfang.ding
 * @date 2019/7/24 13:42
 */
public interface ChannelTransferBizService {

    /**
     * 处理接收消息队列中的数据
     *
     * @param channelTransfer
     */
    void consumerRequestContentQueue(ChannelTransfer channelTransfer);
}
