package com.ddf.scaffold.fw.tcp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;

/**
 * 通道传输报文日志记录接口类
 *
 * @author dongfang.ding
 * @date 2019/7/23 15:41
 */
public interface ChannelTransferService extends IService<ChannelTransfer> {

    /**
     * 根据requestId判断是否已存在记录
     * @param requestId
     * @return
     */
    boolean existByRequestId(String requestId);
}
