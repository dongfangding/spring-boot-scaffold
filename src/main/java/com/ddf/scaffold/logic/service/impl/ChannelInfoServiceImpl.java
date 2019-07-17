package com.ddf.scaffold.logic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.scaffold.fw.keepalive.server.ChannelMonitor;
import com.ddf.scaffold.logic.entity.ChannelInfo;
import com.ddf.scaffold.logic.mapper.ChannelInfoMapper;
import com.ddf.scaffold.logic.service.ChannelInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 连接信息实现类
 *
 * @author dongfang.ding
 * @date 2019/7/17 10:10
 */
@Service
public class ChannelInfoServiceImpl extends ServiceImpl<ChannelInfoMapper, ChannelInfo> implements ChannelInfoService {

    /**
     * 查询所有在线且有效（必须有设备id）的连接
     * @return
     */
    @Override
    public List<ChannelInfo> listOnlineValid() {
        LambdaQueryWrapper<ChannelInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.isNotNull(ChannelInfo::getDeviceId);
        queryWrapper.eq(ChannelInfo::getStatus, ChannelMonitor.STATUS_ACTIVE);
        return list(queryWrapper);
    }
}
