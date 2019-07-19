package com.ddf.scaffold.logic.controller;

import com.ddf.scaffold.logic.model.entity.ChannelInfo;
import com.ddf.scaffold.logic.service.ChannelInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 连接信息控制器
 *
 * @author dongfang.ding
 * @date 2019/7/17 10:15
 */
@RestController
@RequestMapping("/channelInfo")
@Api("连接信息控制器")
public class ChannelInfoController {
    @Autowired
    private ChannelInfoService channelInfoService;

    /**
     * 查询所有在线且有效（必须有设备id）的连接
     * @return
     */
    @RequestMapping("listOnlineValid")
    @ApiOperation("查询所有在线且有效（必须有设备id）的连接")
    public List<ChannelInfo> listOnlineValid() {
        return channelInfoService.listOnlineValid();
    }
}
