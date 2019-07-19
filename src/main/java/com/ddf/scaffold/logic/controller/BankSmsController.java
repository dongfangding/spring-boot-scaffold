package com.ddf.scaffold.logic.controller;

import com.ddf.scaffold.logic.model.bo.BankSmsBO;
import com.ddf.scaffold.logic.model.entity.BankSms;
import com.ddf.scaffold.logic.service.BankSmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 银行短信记录控制器
 *
 * @author dongfang.ding
 * @date 2019/7/19 14:39
 */
@RestController
@RequestMapping("/bankSms")
@Api("银行短信记录控制器")
public class BankSmsController {
    @Autowired
    private BankSmsService bankSmsService;


    /**
     * 查询银行短信记录
     * @param bankSmsBO
     * @return
     */
    @RequestMapping("listBankSms")
    @ApiOperation("查询银行短信记录")
    public List<BankSms> listBankSms(@RequestBody @ApiParam BankSmsBO bankSmsBO) {
        return bankSmsService.listBankSms(bankSmsBO);
    }
}
