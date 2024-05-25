package com.bbs.financial.api.certificate.template;

import com.bbs.Result;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.service.CertificateTemplateService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping
@RestController
public class UpdateTemplate {

    @Resource
    private CertificateTemplateService certificateTemplateService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;


    @PutMapping("/certificate/template/status")
    public Result<Boolean> update(@RequestParam("id") Long id, @RequestParam("isActive") Boolean isActive, @RequestParam("companyId") Long companyId) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            certificateTemplateService.lambdaUpdate()
                    .set(CertificateTemplate::getIsActive,isActive)
                    .eq(CertificateTemplate::getId,id)
                    .eq(CertificateTemplate::getCompanyId,companyId)
                    .update();
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}
