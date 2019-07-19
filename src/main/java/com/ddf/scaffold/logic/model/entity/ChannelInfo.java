package com.ddf.scaffold.logic.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ddf.scaffold.fw.entity.BaseDomain;
import com.ddf.scaffold.fw.keepalive.server.ChannelMonitor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 连接信息实体类
 *
 * @author dongfang.ding
 * @date 2019/7/16 12:50
 */
@Data
@ToString(callSuper = true)
@TableName(value = "log_channel_info")
@ApiModel
public class ChannelInfo extends BaseDomain {
    @ApiModelProperty("设备id")
    private String deviceId;

    @ApiModelProperty("客户端远程地址")
    private String remoteAddress;

    @ApiModelProperty(value = "连接状态", allowableValues="1, 2, 3")
    private Integer status;

    @ApiModelProperty("连接注册时间")
    private Date registryTime;

    @ApiModelProperty("连接状态最后一次变更时间，不是数据变更时间，只针对状态变化")
    private Date changeTime;

    public static ChannelInfo build(ChannelMonitor channelMonitor) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setDeviceId(channelMonitor.getDeviceId());
        channelInfo.setRemoteAddress(channelMonitor.getRemoteAddress());
        channelInfo.setStatus(channelMonitor.getStatus());
        channelInfo.setRegistryTime(channelMonitor.getRegistryTime());
        channelInfo.setChangeTime(channelMonitor.getModifyTime());
        return channelInfo;
    }
}
