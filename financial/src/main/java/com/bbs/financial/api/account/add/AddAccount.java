package com.bbs.financial.api.account.add;

import com.bbs.Result;
import com.bbs.financial.converter.AccountConverter;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.AccountAuxiliary;
import com.bbs.financial.entity.AccountCurrency;
import com.bbs.financial.service.AccountAuxiliaryService;
import com.bbs.financial.service.AccountCurrencyService;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.util.LoginUser;
import com.google.common.base.Preconditions;
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
        private String cashPay;

        /**
         * 辅助核算段
         */
        private String auxiliaryCalculation;

        /**
         * 辅助核算
         */
        private List<String> auxiliaryNameAccount;

        /**
         * 是否数量核算
         */
        private String quantitativeAccount;

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

        /**
         * 数量核算: 计量单位
         */
        private String measurementUnit;
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
    public Result<Long> add(@RequestBody Param param) {
        Account account = converter.toEntity(param);
        String no = param.getNo();
        boolean isChild = no.indexOf("-") > -INTEGER_ONE;
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);


        try {
            if(isChild) {
                String[] split = no.split("-");
                Integer level = split.length - INTEGER_ONE;
                no = split[INTEGER_ZERO];
                Account parent = db.lambdaQuery().eq(Account::getNo, no).eq(Account::getLevel, level - INTEGER_ONE).one();
                Preconditions.checkArgument(nonNull(parent), "父级科目为空，无法添加");
                Long peerLevel = db.lambdaQuery()
                        .eq(Account::getNo, no)
                        .eq(Account::getLevel, level)
                        .count();
                account.setWeight(peerLevel.intValue());
                account.setNo(no);
                account.setParentId(parent.getId());
                account.setAccountName(parent.getAccountName());
                account.setLevel(level);
            }
            db.save(account);
            accountAuxiliaryService.saveBatch(param.getAuxiliaryNameAccount().stream().map(name -> {
                AccountAuxiliary accountAuxiliary = new AccountAuxiliary();
                accountAuxiliary.setAccountId(account.getId());
                accountAuxiliary.setName(name);
                return accountAuxiliary;
            }).collect(Collectors.toList()));
            accountCurrencyService.saveBatch(param.getCurrencyList().stream().peek(currency -> {
                currency.setCompanyId(LoginUser.getCompanyId());
                currency.setCreateBy(LoginUser.getId());
            }).collect(Collectors.toList()));
            transactionManager.commit(transaction);
            return Result.success(account.getId());
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new RuntimeException(e);
        }
    }
}
