package com.bbs.financial.api.invoice.add;

import com.bbs.Result;
import com.bbs.financial.converter.InvoiceConverter;
import com.bbs.financial.entity.AccountAuxiliary;
import com.bbs.financial.entity.AccountAuxiliaryType;
import com.bbs.financial.service.AccountAuxiliaryService;
import com.bbs.financial.service.AccountAuxiliaryTypeService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 发票侧新增辅助核算
 */
@RestController
@RequestMapping
public class AddAuxByInovice {
    @Resource
    private DataSourceTransactionManager transactionManager;

    @Resource
    private TransactionDefinition transactionDefinition;

    @Resource
    private InvoiceConverter invoiceConverter;

    @Resource
    private AccountAuxiliaryService orm;

    @Resource
    private AccountAuxiliaryTypeService typeService;

    private class StringTIP {
        public static final String SAVE_AUX = "存货";

        public static final String NO_SAVE = "没有名为存货的辅助核算类型";

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        /**
         * 编码
         */
        private String no;

        /**
         * 名称
         */
        private String name;

        /**
         * 备注
         */
        private String remark;

        /**
         * 核算类型id
         */
        private Long typeId;
    }

    /**
     * 发票侧新增辅助核算
     */
    @PutMapping("/invoice/auxiliary")
    public Result<Boolean> add(@RequestBody AddAuxByInovice.Param param) {
        AccountAuxiliary auxiliary = invoiceConverter.toEntity(param);

        Long typeId = auxiliary.getTypeId();

        //新增发票明细的辅助核算
        if (Objects.isNull(typeId) || typeId < NumberUtils.LONG_ONE) {
            typeId = typeService.selectJoinOne(Long.class, new MPJLambdaWrapper<AccountAuxiliaryType>()
                    .select(AccountAuxiliaryType::getId)
                    .eq(AccountAuxiliaryType::getAccountingSetId, LoginUser.getLoginSetId())
                    .eq(AccountAuxiliaryType::getIsUserDefined, NumberUtils.INTEGER_ZERO)
                    .eq(AccountAuxiliaryType::getName, StringTIP.SAVE_AUX));
        }

        //与门右侧判断是否是新增发票明细时的辅助核算
        if ((Objects.isNull(typeId) || typeId < NumberUtils.LONG_ONE) && Objects.isNull(param.getTypeId()))
            return Result.failed(StringTIP.NO_SAVE);

        //辅助核算赋值并入库
        auxiliary.setAccountingSetId(LoginUser.getLoginSetId());
        auxiliary.setTypeId(typeId);

        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            orm.save(auxiliary);
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}