package com.bbs.financial.api.invoice.delete;

import com.bbs.Result;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping
@RestController
public class DeleteInvoice {
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private CertificateService certORM;

    @Resource
    private CertificateAbstractService abstORM;

    @Resource
    private CertificateFileService fileORM;

    @Resource
    private InvoiceService orm;

    @Resource
    private InvoiceDetailService detailORM;

    @DeleteMapping("/invoice/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.lambdaUpdate()
                    .eq(Invoice::getId, id)
                    .remove();

            detailORM.lambdaUpdate()
                    .eq(InvoiceDetail::getInvoiceId, id)
                    .remove();

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
    }

    @DeleteMapping("/invoice/cert/{id}/{certId}")
    public Result<Boolean> removeCert(@PathVariable Long id, @PathVariable Long certId) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            //凭证相关删除
            certORM.removeById(certId);
            abstORM.lambdaUpdate()
                    .eq(CertificateAbstract::getCertificateId, certId)
                    .remove();
            fileORM.lambdaUpdate()
                    .eq(CertificateFile::getCertificateId, certId)
                    .remove();

            //修改发票
            orm.lambdaUpdate()
                    .set(Invoice::getCertificateId, null)
                    .set(Invoice::getTempName, null)
                    .eq(Invoice::getId, id)
                    .update();

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
    }
}