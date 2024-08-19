package com.bbs.financial.api.note.delete;

import com.bbs.Result;

import com.bbs.financial.controller.CertificateController;
import com.bbs.financial.entity.Note;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.util.ORMUtil;
import com.bbs.financial.util.SpringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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
    private ApplicationContext appContext;

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
        return ORMUtil.fastTran(() -> {
            SpringUtil.getRespData(CertificateController.class, appContext, c -> c.remove(certId));

            orm.lambdaUpdate()
                    .set(Note::getCertificateId, null)
                    .eq(Note::getId, id)
                    .update();
        }, transactionManager, transactionDefinition);
    }

    /**
     * 删除日记账
     *
     * @param id 日记账id
     */
    @DeleteMapping("/note/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return ORMUtil.fastTran(() -> orm.removeById(id), transactionManager, transactionDefinition);
    }
}