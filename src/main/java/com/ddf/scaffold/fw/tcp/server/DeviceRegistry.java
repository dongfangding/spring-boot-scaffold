package com.ddf.scaffold.fw.tcp.server;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备注册报文格式
 *
 * @author dongfang.ding
 * @date 2019/8/13 17:09
 */
@Data
public class DeviceRegistry implements Serializable {

    /**
     * 设备号
     */
    private String deviceNo;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 设备版本
     */
    private String deviceVersion;

    /**
     * 设备名称
     */
    private String deviceName;
}
