package com.bbs.financial.api.invoice.add;

import com.bbs.Result;
import com.bbs.financial.controller.InvoiceController;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.util.SpringUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class AddDefaultTemplate {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private CertificateTemplateService orm;

    /**
     * 设置默认模板
     *
     * @param invoiceCategory 发票分类：0.销项发票;1.进项发票;2.费用小票;
     * @param tempId          模板id
     */
    @GetMapping("/invoice/template/default/{invoiceCategory}/{tempId}")
    public Result<Boolean> setDefaultTemplate(@PathVariable Integer invoiceCategory, @PathVariable Long tempId) {
        Long outTempId = SpringUtil.getRespData(InvoiceController.class, applicationContext, i -> i.defaultByTemplateId(invoiceCategory));

        if (tempId.equals(outTempId))
            return Result.success();

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            //原默认模板修改为非默认
            if (!ObjectUtils.isEmpty(outTempId)) {
                orm.lambdaUpdate()
                        .set(CertificateTemplate::getIsDefault, false)
                        .eq(CertificateTemplate::getId, outTempId)
                        .update();
            }

            //将当前默认设为默认
            orm.lambdaUpdate()
                    .set(CertificateTemplate::getIsDefault, true)
                    .eq(CertificateTemplate::getId, tempId)
                    .update();

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}