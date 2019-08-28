package com.ddf.scaffold.fw.tcp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.scaffold.fw.tcp.mapper.ChannelInfoMapper;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelInfo;
import com.ddf.scaffold.fw.tcp.server.ChannelMonitor;
import com.ddf.scaffold.fw.tcp.service.ChannelInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
     *
     * @return
     */
    @Override
    public List<ChannelInfo> listOnlineValid() {
        LambdaQueryWrapper<ChannelInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.isNotNull(ChannelInfo::getDeviceNo);
        queryWrapper.eq(ChannelInfo::getStatus, ChannelMonitor.STATUS_ACTIVE);
        return list(queryWrapper);
    }


    /**
     * 将所有的连接失效
     * 如果服务端异常终止，是来不及更新所有在线的连接状态的，如果启动后这些数据都会出现问题
     * 提供一个方法，将所有连接失效掉；让客户端重连
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void invalidConnection() {
        LambdaUpdateWrapper<ChannelInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(ChannelInfo::getStatus, ChannelMonitor.STATUS_INACTIVE);
        update(updateWrapper);
    }


    /**
     * 检测设备是否在线
     *
     * @param deviceNo
     * @return 0 不在线  1 在线
     */
    @Override
    public Integer checkOnline(String deviceNo) {
        if (StringUtils.isEmpty(deviceNo)) {
            return 0;
        }
        LambdaQueryWrapper<ChannelInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChannelInfo::getDeviceNo, deviceNo);
        queryWrapper.eq(ChannelInfo::getStatus, ChannelMonitor.STATUS_ACTIVE);
        return count(queryWrapper) > 0 ? 1 : 0;
    }
}
