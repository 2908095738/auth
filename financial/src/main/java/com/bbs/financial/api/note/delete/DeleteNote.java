package com.bbs.financial.api.note.delete;

import com.bbs.Result;

import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.entity.Note;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.service.CertificateService;
import com.bbs.financial.service.NoteService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping
public class DeleteNote {

    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private CertificateService certORM;

    @Resource
    private CertificateAbstractService abstORM;

    @Resource
    private NoteService orm;

    /**
     * 删除日记账凭证
     *
     * @param id     日记账id
     * @param certId 凭证id
     */
    @DeleteMapping("/note/cert/{id}/{certId}")
    public Result<Boolean> removeCert(@PathVariable Long id, @PathVariable Long certId) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            //凭证相关删除
            certORM.removeById(certId);
            abstORM.lambdaUpdate()
                    .eq(CertificateAbstract::getCertificateId, certId)
                    .remove();

            orm.lambdaUpdate()
                    .set(Note::getCertificateId, null)
                    .eq(Note::getId, id)
                    .update();

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
    }

    /**
     * 删除日记账
     *
     * @param id 日记账id
     */
    @DeleteMapping("/note/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.removeById(id);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            return Result.failed(e.getMessage());
        }
    }
}