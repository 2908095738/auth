package com.bbs.financial.api.close;

import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.CertificateAbstract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

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
}
