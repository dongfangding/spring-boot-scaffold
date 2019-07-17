package com.ddf.scaffold.logic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.scaffold.logic.entity.ChannelInfo;

import java.util.List;

/**
 * @author dongfang.ding
 * @date 2019/7/17 10:08
 */
public interface ChannelInfoService extends IService<ChannelInfo> {

    /**
     * 查询所有在线且有效（必须有设备id）的连接
     * @return
     */
    List<ChannelInfo> listOnlineValid();
}
