package com.bbs.financial.api.note.add;

import com.bbs.Result;
import com.bbs.financial.converter.NoteConverter;
import com.bbs.financial.entity.Note;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.util.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@RestController
@RequestMapping
public class AddNote {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private NoteService orm;

    @Resource
    private NoteConverter noteConverter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        /**
         * 日期
         */
        private Date date;

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

        /**
         * 备注
         */
        private String remark;

        /**
         * 账户id
         */
        private Long zhId;
    }

    /**
     * 新增日记账
     */
    @PutMapping("/note")
    public Result<Boolean> add(@RequestBody AddNote.Param param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        Note note = noteConverter.toEntity(param);
        try {
            note.setCreateBy(LoginUser.getId());
            note.setCompanyId(LoginUser.getCompanyId());

            orm.save(note);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}