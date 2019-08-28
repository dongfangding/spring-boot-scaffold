package com.ddf.scaffold.fw.tcp.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 存放银行短信解析后的数据
 *
 * @author dongfang.ding
 * @date 2019/8/6 12:37
 */
@Data
@ApiModel("存放银行短信解析后的数据")
public class BankTemplateParse implements Serializable {
    private static final long serialVersionUID = -4641248346841659769L;

    @ApiModelProperty("设备号")
    private String deviceNo;

    @ApiModelProperty("每条短信的唯一标识符")
    private String messageId;

    @ApiModelProperty("银行电话")
    private String bankTel;

    @ApiModelProperty("银行卡后4位尾号")
    private String cardLast4Num;

    @ApiModelProperty("金额转入时间毫秒值")
    private long inTime;

    @ApiModelProperty("存入金额")
    private BigDecimal inAmount;

    @ApiModelProperty("收件号码")
    private String receiverTel;


}
