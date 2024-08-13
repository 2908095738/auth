package com.bbs.financial.api.invoice.update;

import com.bbs.Result;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.service.CertificateTemplateService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 开启凭证模板
 */
@RestController
@RequestMapping
public class EnableTemp {

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private CertificateTemplateService tempORM;

    /**
     * 开启凭证模板
     *
     * @param tempId 凭证模板id
     */
    @GetMapping("/invoice/cert/temp/enable/{tempId}")
    public Result<Boolean> enable(@PathVariable Long tempId) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            tempORM.lambdaUpdate()
                    .set(CertificateTemplate::getIsActive, true)
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