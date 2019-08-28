package com.ddf.scaffold.fw.tcp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.scaffold.fw.tcp.mapper.ChannelTransferMapper;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;
import com.ddf.scaffold.fw.tcp.service.ChannelTransferService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 通道传输报文日志记录实现类
 *
 * @author dongfang.ding
 * @date 2019/7/23 15:41
 */
@Service
public class ChannelTransferServiceImpl extends ServiceImpl<ChannelTransferMapper, ChannelTransfer> implements ChannelTransferService {

    /**
     * 根据requestId判断是否已存在记录
     * @param requestId
     * @return
     */
    @Override
    public boolean existByRequestId(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            // requestId不能为空，为空返回校验不通过
            return true;
        }
        LambdaQueryWrapper<ChannelTransfer> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChannelTransfer::getRequestId, requestId);
        return count(queryWrapper) > 0;
    }
}
