package com.bbs.financial.api.note.update;

import com.bbs.Result;
import com.bbs.financial.converter.NoteConverter;
import com.bbs.financial.entity.Note;
import com.bbs.financial.service.NoteService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@RestController
@RequestMapping
public class UpdateNote {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private NoteConverter noteConverter;

    @Resource
    private NoteService orm;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        /**
         * 日记账唯一标识符
         */
        private Long id;

        /**
         * 日期
         */
        private Date date;

        /**
         * 备注
         */
        private String remark;

        /**
         * 摘要
         */
        private String certificateAbstract;

        /**
         * 账户对方科目id
         */
        private Long heAccountId;

        /**
         * 收入
         */
        private BigDecimal borrowMoney;

        /**
         * 支出
         */
        private BigDecimal loansMoney;
    }

    /**
     * 修改日记账
     */
    @PostMapping("/note")
    public Result<Boolean> update(@RequestBody UpdateNote.Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        Note note = noteConverter.toEntity(param);
        try {
            orm.lambdaUpdate()
                    .set(Note::getDate, note.getDate())
                    .set(Note::getRemark, note.getRemark())
                    .set(Note::getCertificateAbstract, note.getCertificateAbstract())
                    .set(Note::getHeAccountId, note.getHeAccountId())
                    .set(Note::getBorrowMoney, note.getBorrowMoney())
                    .set(Note::getLoansMoney, note.getLoansMoney())

                    .eq(Note::getId, note.getId())
                    .update();

            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}