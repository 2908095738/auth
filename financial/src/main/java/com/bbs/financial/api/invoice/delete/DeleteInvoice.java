package com.bbs.financial.api.invoice.delete;

import com.bbs.Result;
import com.bbs.financial.controller.CertificateController;
import com.bbs.financial.entity.*;
import com.bbs.financial.service.*;
import com.bbs.financial.util.ORMUtil;
import com.bbs.financial.util.SpringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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
    private ApplicationContext appContext;

    @Resource
    private InvoiceService orm;

    @Resource
    private InvoiceDetailService detailORM;

    @DeleteMapping("/invoice/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return ORMUtil.fastTran(() -> {
            orm.lambdaUpdate()
                    .eq(Invoice::getId, id)
                    .remove();

            detailORM.lambdaUpdate()
                    .eq(InvoiceDetail::getInvoiceId, id)
                    .remove();
        }, transactionManager, transactionDefinition);
    }

    @DeleteMapping("/invoice/cert/{id}/{certId}")
    public Result<Boolean> removeCert(@PathVariable Long id, @PathVariable Long certId) {
        return ORMUtil.fastTran(() -> {
            SpringUtil.getRespData(CertificateController.class, appContext, c -> c.remove(certId));

            //修改发票
            orm.lambdaUpdate()
                    .set(Invoice::getCertificateId, null)
                    .set(Invoice::getTempName, null)
                    .eq(Invoice::getId, id)
                    .update();
        }, transactionManager, transactionDefinition);
    }
}