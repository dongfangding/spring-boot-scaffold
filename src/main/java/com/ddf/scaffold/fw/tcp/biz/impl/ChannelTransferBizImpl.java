package com.ddf.scaffold.fw.tcp.biz.impl;

import com.ddf.scaffold.fw.tcp.biz.ChannelTransferBizService;
import com.ddf.scaffold.fw.tcp.model.datao.BankSms;
import com.ddf.scaffold.fw.tcp.model.datao.ChannelTransfer;
import com.ddf.scaffold.fw.tcp.server.ChannelTransferStore;
import com.ddf.scaffold.fw.tcp.server.DeviceRegistry;
import com.ddf.scaffold.fw.tcp.server.RequestContent;
import com.ddf.scaffold.fw.tcp.server.SmsContent;
import com.ddf.scaffold.fw.tcp.service.BankSmsService;
import com.ddf.scaffold.fw.tcp.service.ChannelTransferService;
import com.ddf.scaffold.fw.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 通道传输日志处理业务层实现类
 *
 * @author dongfang.ding
 * @date 2019/7/24 13:44
 */
@Service
@Slf4j
public class ChannelTransferBizImpl implements ChannelTransferBizService {

    @Autowired
    private ChannelTransferService channelTransferService;
    @Autowired
    private BankSmsService bankSmsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void consumerRequestContentQueue(ChannelTransfer channelTransfer) {
        if (null == channelTransfer) {
            return;
        }
        RequestContent requestContent = channelTransfer.getRequestContent();
        if (requestContent == null) {
            // 如果是第一次处理数据的话，内存中会有数据，如果内存中没有，可能就是从数据库中取出来的数据了，需要转换一下
            requestContent = JsonUtil.toBean(channelTransfer.getContent(), RequestContent.class);
        }
        if (requestContent.getBody() == null || !checkChannelTransferValid(channelTransfer)) {
            return;
        }
        boolean delayResponse = false;
        // 处理短信上传
        if (RequestContent.Cmd.SMS_UPLOAD.name().equals(requestContent.getCmd())) {
            String deviceNo = requestContent.getDeviceNo();
            if (deviceNo == null) {
                return;
            }
            List<Map<String, Object>> smsList = JsonUtil.toBean(requestContent.getBody(), List.class);
            if (smsList != null && !smsList.isEmpty()) {
                for (Map<String, Object> map : smsList) {
                    SmsContent smsContent = JsonUtil.toBean(JsonUtil.asString(map), SmsContent.class);
                    BankSms bankSms = new BankSms();
                    bankSms.setDeviceNo(deviceNo);
                    bankSms.setMessageId(smsContent.getMessageId());
                    bankSms.setClientAddress(channelTransfer.getClientAddress());
                    bankSms.setSender(smsContent.getSender());
                    bankSms.setReceiver(smsContent.getReceiver());
                    bankSms.setReceiveTime(new Date(smsContent.getReceiverTime()));
                    bankSms.setContent(smsContent.getContent());
                    bankSmsService.save(bankSms);
                }
            }
        } else if (RequestContent.Cmd.DEVICE_REGISTRY.name().equals(requestContent.getCmd())) {
            delayResponse = true;
            // 设备注册
            DeviceRegistry deviceRegistry = JsonUtil.toBean(requestContent.getBody(), DeviceRegistry.class);
            if (checkDeviceRegistry(deviceRegistry)) {
               // do something
            }
        }
        // 更新日志为处理完成
        ChannelTransfer updateChannelTransfer = new ChannelTransfer();
        updateChannelTransfer.setStatusVerifiedAndFinished();
        updateChannelTransfer.setId(channelTransfer.getId());
        channelTransferService.updateById(updateChannelTransfer);
        if (delayResponse) {
            // 设备注册只有当消费成功后才响应成功
            ChannelTransferStore.get(channelTransfer.getClientAddress()).getChannel()
                    .writeAndFlush(RequestContent.responseOK(channelTransfer.getRequestContent()));
        }
    }


    /**
     * 校验当前日志报文数据是否需要处理
     * 1. 必须验签通过
     *
     * @param channelTransfer
     * @return
     */
    private boolean checkChannelTransferValid(ChannelTransfer channelTransfer) {
        if (!ChannelTransfer.waitToDeal().equals(channelTransfer.getStatus())) {
            return false;
        }
        return true;
    }

    /**
     * 校验设备注册数据
     * @param deviceRegistry
     * @return
     */
    private boolean checkDeviceRegistry(DeviceRegistry deviceRegistry) {
        if (deviceRegistry == null || StringUtils.isAnyBlank(deviceRegistry.getDeviceNo(), deviceRegistry.getDeviceModel(),
                deviceRegistry.getDeviceModel(), deviceRegistry.getDeviceVersion())) {
            log.error("设备注册数据校验不通过： {}", deviceRegistry);
            return false;
        }
        return true;
    }
}
