package com.bbs.financial.controller;

import com.bbs.Result;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 发票控制器
 */
@Slf4j
@Controller
@RequestMapping("/invoice")
public class InvoiceController {
    @Resource
    private CertificateTemplateService templateService;

    /**
     * 获取默认发票凭证模板id
     *
     * @param invoiceCategory 发票分类：0.销项发票;1.进项发票;2.费用小票;
     */
    @ResponseBody
    @GetMapping("/defaultTemplateId")
    public Result<Long> defaultByTemplateId(@RequestParam Integer invoiceCategory) {
        return Result.success(templateService.selectJoinOne(Long.class,
                new MPJLambdaWrapper<CertificateTemplate>()
                        .select(CertificateTemplate::getId)
                        .eq(CertificateTemplate::getAccountingSetId, LoginUser.getLoginSetId())
                        .eq(CertificateTemplate::getInvoiceCategory, invoiceCategory)
                        .eq(CertificateTemplate::getIsDefault, Boolean.TRUE)
                        .eq(CertificateTemplate::getIsActive, Boolean.TRUE)));
    }
}