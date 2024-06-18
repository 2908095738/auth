package com.bbs.financial.api.certificate.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import com.bbs.financial.service.CertificateTemplateAbstractService;
import com.bbs.financial.service.CertificateTemplateService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping
@RestController
public class DelTemplate {

    @Resource
    private CertificateTemplateService certificateTemplateService;
    @Resource
    private CertificateTemplateAbstractService certificateTemplateAbstractService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long id;
    }

    @DeleteMapping("/certificate/template")
    public Result<Boolean> del(@RequestBody Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            certificateTemplateAbstractService.remove(new LambdaQueryWrapper<CertificateTemplateAbstract>()
                    .eq(CertificateTemplateAbstract::getTemplateId, param.id)
            );
            certificateTemplateService.removeById(param.id);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}
