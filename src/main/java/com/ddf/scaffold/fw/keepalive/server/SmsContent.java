package com.ddf.scaffold.fw.keepalive.server;

import lombok.Data;

import java.util.Date;

/**
 * 短信上传body报文格式
 *
 * @author dongfang.ding
 * @date 2019/7/19 12:58
 */
@Data
public class SmsContent {

    /**
     * 发件人，填号码
     */
    private String sender;
    /**
     * 收件人
     */
    private String receiver;

    /**
     * 收件时间
     */
    private Date receiverTime;

    /**
     * 短信内容
     */
    private String content;

}
