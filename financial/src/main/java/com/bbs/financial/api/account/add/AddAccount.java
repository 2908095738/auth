package com.bbs.financial.api.account.add;

import com.bbs.Result;
import com.bbs.financial.converter.AccountConverter;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.AccountAuxiliary;
import com.bbs.financial.entity.AccountCurrency;
import com.bbs.financial.service.AccountAuxiliaryService;
import com.bbs.financial.service.AccountCurrencyService;
import com.bbs.financial.service.AccountService;
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
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@RestController
@RequestMapping
public class AddAccount {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        private String no;

        private String name;

        private Long parentId;

        /**
         * 公司ID
         */
        private Long companyId;

        /**
         * 科目类别
         */
        private String accountSort;

        /**
         * 类别
         */
        private String sort;

        /**
         * 方向
         */
        private String direction;

        /**
         * 是否现金支付
         */
        private Integer cashPay;

        /**
         * 辅助核算段
         */
        private String auxiliaryCalculation;

        /**
         * 辅助核算
         */
        private List<String> auxiliaryAccount;

        /**
         * 是否数量核算
         */
        private Integer quantitativeAccount;

        /**
         * 数量核算单位
         */
        private String quantityAccountUnit;


        /**
         * 是否外币核算
         */
        private String currency;

        /**
         * 是否期末调汇
         */
        private String periodExchangeRateAdjust;

        /**
         * 外币核算币别
         */
        private List<AccountCurrency> currencyList;

        /**
         * 级别
         */
        private Integer level;
    }

    @Resource
    private AccountConverter converter;

    @Resource
    private AccountService db;

    @Resource
    private AccountAuxiliaryService accountAuxiliaryService;

    @Resource
    private AccountCurrencyService accountCurrencyService;

    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;

    @PutMapping("/account")
    public Result<Boolean> add(@RequestBody Param param) {
        Account account = converter.toEntity(param);
        Long parentId = account.getParentId();
        Integer level = param.getLevel();
        String no = param.getNo();
        String[] split = no.split("-");
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);


        try {
            if(nonNull(parentId) && nonNull(level)) {
                Long peerLevel = db.lambdaQuery()
                        .eq(Account::getNo, split[INTEGER_ZERO])
                        .eq(Account::getLevel, level)
                        .count();
                account.setWeight(peerLevel.intValue() + INTEGER_ONE);
            }
            db.save(account);
            accountAuxiliaryService.saveBatch(param.getAuxiliaryAccount().stream().map(auxiliaryAccountName -> {
                AccountAuxiliary accountAuxiliary = new AccountAuxiliary();
                accountAuxiliary.setAccountId(account.getId());
                accountAuxiliary.setName(auxiliaryAccountName);
                return accountAuxiliary;
            }).collect(Collectors.toList()));
            accountCurrencyService.saveBatch(param.getCurrencyList());
            transactionManager.commit(transaction);
            return Result.success();
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}
