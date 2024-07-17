package com.bbs.financial.api.close.balance;

import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.CertificateAbstract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static cn.hutool.core.lang.Opt.ofNullable;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

/**
 * 试算平衡表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialBalance {

    /**
     * 数据
     */
    private List<Item> data;

    /**
     * 总计：借方金额
     */
    private Long countBorrowMoney;

    /**
     * 总计：贷方金额
     */
    private Long countLoansMoney;

    /**
     * 是否平衡
     */
    private Boolean isBalance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        /**
         * 科目名称
         */
        private String accountName;

        /**
        * 借方金额
        */
        private Long borrowMoney;

        /**
        * 贷方金额
        */
        private Long loansMoney;

        public Item(Account account, CertificateAbstract certificateAbstract) {
            this.accountName = account.getAccountName();
            this.borrowMoney = Optional.of(certificateAbstract.getBorrowMoney()).orElseGet(LONG_ZERO::longValue);
            this.loansMoney = Optional.of(certificateAbstract.getLoansMoney()).orElseGet(LONG_ZERO::longValue);
        }

        public void setBorrowMoney(CertificateAbstract certificateAbstract) {
            this.borrowMoney = ofNullable(borrowMoney).orElseGet(LONG_ZERO::longValue)
                    +
                    ofNullable(certificateAbstract.getBorrowMoney()).orElseGet(LONG_ZERO::longValue)
            ;
        }

        public void setLoansMoney(CertificateAbstract certificateAbstract) {
            this.loansMoney = ofNullable(loansMoney).orElseGet(LONG_ZERO::longValue)
                    +
                    ofNullable(certificateAbstract.getLoansMoney()).orElseGet(LONG_ZERO::longValue)
            ;
        }
    }


    /**
     * 编制试算平衡表
     */
    public static TrialBalance generateTrialBalance(List<CertificateAbstract> initialData) {
        // 通过 Set 集合与科目的 no 字段（顶级科目的编号，相同父级科目的 no 相同）
        long countBorrowMoney = LONG_ZERO;
        long countLoansMoney = LONG_ZERO;
        Map<String, Item> accountNoAndTrialBalanceItemMaps = new HashMap<>();
        for (CertificateAbstract certificateAbstract : initialData) {
            Account account = certificateAbstract.getAccount();
            String no = account.getNo();
            TrialBalance.Item trialBalanceItem;
            if(accountNoAndTrialBalanceItemMaps.containsKey(no)) {
                trialBalanceItem = accountNoAndTrialBalanceItemMaps.get(no);
                // 求和借方金额：科目借方金额 = 凭证借方金额 + 科目借方金额
                trialBalanceItem.setBorrowMoney(certificateAbstract);
                // 求和贷方金额：科目贷方金额 = 凭证贷方金额 + 科目贷方金额
                trialBalanceItem.setLoansMoney(certificateAbstract);
            } else {
                trialBalanceItem = new TrialBalance.Item(account, certificateAbstract);
                accountNoAndTrialBalanceItemMaps.put(no, trialBalanceItem);
            }
            // 求和总计借/贷方金额
            countBorrowMoney += trialBalanceItem.getBorrowMoney();
            countLoansMoney += trialBalanceItem.getLoansMoney();
        }
        boolean isBalance = countBorrowMoney == countLoansMoney;
        return new TrialBalance(new ArrayList<>(accountNoAndTrialBalanceItemMaps.values()), countBorrowMoney, countLoansMoney, isBalance);
    }
}
