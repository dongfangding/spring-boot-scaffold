package com.ddf.scaffold.fw.tcp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelInfo;

import java.util.List;

/**
 * @author dongfang.ding
 * @date 2019/7/17 10:08
 */
public interface ChannelInfoService extends IService<ChannelInfo> {

    /**
     * 查询所有在线且有效（必须有设备id）的连接
     *
     * @return
     */
    List<ChannelInfo> listOnlineValid();

    /**
     * 将所有的连接失效
     * 如果服务端异常终止，是来不及更新所有在线的连接状态的，如果启动后这些数据都会出现问题
     * 提供一个方法，将所有连接失效掉；让客户端重连
     */
    void invalidConnection();


    /**
     * 检测设备是否在线
     *
     * @param deviceNo
     * @return 0 不在线  1 在线
     */
    Integer checkOnline(String deviceNo);

}
