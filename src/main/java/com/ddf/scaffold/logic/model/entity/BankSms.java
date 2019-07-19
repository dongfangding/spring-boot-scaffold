package com.ddf.scaffold.logic.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ddf.scaffold.fw.entity.BaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author {@link EntityGenerateUtil} Fri Jul 19 11:30:20 CST 2019
 */
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("message_bank_sms")
@Data
@ApiModel("银行收款短信记录表")
public class BankSms extends BaseDomain {

    /**
     * 短信的设备id
     */
    @ApiModelProperty("短信的设备id")
    private String deviceId;

    /**
     * 设备的远程ip地址
     */
    @ApiModelProperty("设备的远程ip地址")
    private String remoteAddress;

    /**
     * 发送方号码，暂定拿5位号码验证，必须是5位，或者维护所有银行的客服号码，做一个校验，因为后面也需要知道每个银行的短信模板
     * 与订单服务对接时，这个值也需要；
     * 订单服务需要校验收件号码和设备id和金额同时满足同一个人
     */
    @ApiModelProperty("发送方号码")
    private String sender;

    @ApiModelProperty("收件方号码")
    private String receiver;

    @ApiModelProperty("收件时间，以安卓设备短信时间为准")
    private Date receiveTime;

    @ApiModelProperty("短信内容")
    private String content;
}