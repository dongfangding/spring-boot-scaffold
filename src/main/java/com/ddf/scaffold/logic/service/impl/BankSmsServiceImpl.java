package com.ddf.scaffold.logic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.scaffold.logic.mapper.BankSmsMapper;
import com.ddf.scaffold.logic.model.bo.BankSmsBO;
import com.ddf.scaffold.logic.model.entity.BankSms;
import com.ddf.scaffold.logic.service.BankSmsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 银行收款短信记录接口实现类
 *
 * @author dongfang.ding
 * @date 2019/7/19 11:36
 */
@Service
public class BankSmsServiceImpl extends ServiceImpl<BankSmsMapper, BankSms> implements BankSmsService {

    /**
     * 查询银行短信记录
     * @param bankSmsBO
     * @return
     */
    @Override
    public List<BankSms> listBankSms(BankSmsBO bankSmsBO) {
        LambdaQueryWrapper<BankSms> queryWrapper = Wrappers.lambdaQuery();
        if (bankSmsBO.getDeviceId() != null) {
            queryWrapper.eq(BankSms::getDeviceId, bankSmsBO.getDeviceId());
        }
        if (bankSmsBO.getSender() != null) {
            queryWrapper.eq(BankSms::getSender, bankSmsBO.getSender());
        }
        if (bankSmsBO.getReceiver() != null) {
            queryWrapper.eq(BankSms::getReceiver, bankSmsBO.getReceiver());
        }
        if (bankSmsBO.getReceiveTimeFrom() != null && bankSmsBO.getReceiveTimeTo() != null) {
            queryWrapper.between(BankSms::getReceiveTime, bankSmsBO.getReceiveTimeFrom(), bankSmsBO.getReceiveTimeTo());
        }
        return list(queryWrapper);
    }
}
