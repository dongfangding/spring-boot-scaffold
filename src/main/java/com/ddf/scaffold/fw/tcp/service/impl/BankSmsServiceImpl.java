package com.ddf.scaffold.fw.tcp.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.scaffold.fw.tcp.mapper.BankSmsMapper;
import com.ddf.scaffold.fw.tcp.model.datao.BankSms;
import com.ddf.scaffold.fw.tcp.service.BankSmsService;
import org.springframework.stereotype.Service;

/**
 * 银行收款短信记录接口实现类
 *
 * @author dongfang.ding
 * @date 2019/7/19 11:36
 */
@Service
public class BankSmsServiceImpl extends ServiceImpl<BankSmsMapper, BankSms> implements BankSmsService {

}
