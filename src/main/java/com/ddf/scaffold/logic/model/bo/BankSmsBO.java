package com.ddf.scaffold.logic.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信记录查询参数对象
 *
 * @author dongfang.ding
 * @date 2019/7/19 14:16
 */
@ApiModel("短信记录查询参数对象")
@Data
public class BankSmsBO implements Serializable {

    private static final long serialVersionUID = -7310082963979865835L;

    @ApiModelProperty("设备id")
    private String deviceId;

    @ApiModelProperty("短信发送方")
    private String sender;

    @ApiModelProperty("短信接收方")
    private String receiver;

    @ApiModelProperty("短信接收起始时间")
    private Date receiveTimeFrom;

    @ApiModelProperty("短信接收起始时间")
    private Date receiveTimeTo;
}
