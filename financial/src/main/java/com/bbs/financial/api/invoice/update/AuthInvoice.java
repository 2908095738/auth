package com.bbs.financial.api.invoice.update;

import com.bbs.Result;
import com.bbs.financial.entity.Invoice;
import com.bbs.financial.service.InvoiceService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 发票认证
 */
@RestController
@RequestMapping
public class AuthInvoice {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private InvoiceService orm;

    /**
     * 发票认证
     *
     * @param invId 发票id
     */
    @GetMapping("/invoice/auth/{invId}")
    public Result<Boolean> auth(@PathVariable Long invId) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.lambdaUpdate()
                    .set(Invoice::getIsAuth, true)
                    .eq(Invoice::getId, invId)
                    .update();

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}