package com.bbs.financial.api.accountBook;

import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 账簿-数量金额总账
 */
@RestController
public class LedgerController {

    @Resource
    private CertificateAbstractService certificateAbstractService;


    @GetMapping("/certificate/account/ledger")
    public Result<List<Vo>> ledgerAccount(@RequestParam("createTime") String certificateCreateTime) {
        Long loginSetId = LoginUser.getLoginSetId();
        List<CertificateAbstract> list = certificateAbstractService.selectQuantityAmountList(loginSetId, certificateCreateTime, null);
        certificateAbstractService.initDataByNo(list);
        return Result.success(null);
    }


    @Data
    private static class Vo {
        /**
         * 科目id
         */
        private Long accountId;

        /**
         * 科目名称
         */
        private String accountName;


        /**
         * 科目编号
         */
        private Long accountNo;

        /**
         * 单位
         */
        private String unit;


        /**
         * 期初余额
         */

        private Integer initialBalanceNum;

        private Long initialBalancePrice;



        /**
         * 本期发生额-借方
         */

        private Integer currentPeriodBorrowNum;

        private Long currentPeriodBorrowMoney;


        /**
         * 本期发生额-贷方
         */

        private Integer currentPeriodLoansNum;

        private Long currentPeriodLoansMoney;



        /**
         * 本年累计发生额-借方
         */

        private Integer incurredYearBorrowNum;

        private Long incurredYearBorrowMoney;



        /**
         * 本年累计发生额-贷方
         */

        private Integer incurredYearLoansNum;

        private Long incurredYearLoansMoney;



        /**
         * 期末余额
         */

        private Integer endingBalanceNum;

        private Long endingBalanceMoney;

        private Long endingBalancePrice;


    }


}
