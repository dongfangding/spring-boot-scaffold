package com.ddf.scaffold.logic.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.scaffold.logic.model.bo.BankSmsBO;
import com.ddf.scaffold.logic.model.entity.BankSms;

import java.util.List;


/**
 * 银行收款短信记录接口
 *
 * @author dongfang.ding
 * @date 2019/7/19 11:35
 */
public interface BankSmsService extends IService<BankSms> {

    /**
     * 查询银行短信记录
     * @param bankSmsBO
     * @return
     */
    List<BankSms> listBankSms(BankSmsBO bankSmsBO);
}
