package com.ddf.scaffold.fw.tcp.server;

import lombok.Data;

/**
 * 短信上传body报文格式
 *
 * @author dongfang.ding
 * @date 2019/7/19 12:58
 */
@Data
public class SmsContent {
    /**
     * 短信的唯一码
     */
    private String messageId;

    /**
     * 发件人，填号码
     */
    private String sender;
    /**
     * 收件人，填号码
     */
    private String receiver;

    /**
     * 收件时间毫秒值
     */
    private Long receiverTime;

    /**
     * 短信内容
     */
    private String content;

}
