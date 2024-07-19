package com.bbs.financial.api.accountBook;

import cn.hutool.core.collection.CollUtil;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        List<CertificateAbstract> list = certificateAbstractService.selectQuantityAmountList(LoginUser.getLoginSetId(), certificateCreateTime, null);
        List<Vo> result = new ArrayList<>();
        if(CollUtil.isNotEmpty(list)){
            certificateAbstractService.initDataByMonth(list);
            for (CertificateAbstract anAbstract : list) {
                Vo vo = new Vo();
                vo.setAccountId(anAbstract.getAccountId());

                if(Objects.nonNull(anAbstract.getAccountAuxiliary())){
                    vo.setAccountName(anAbstract.getAccountAuxiliary().getName());
                    vo.setAccountNo(anAbstract.getAccountAuxiliary().getNo());
                    vo.setUnit(anAbstract.getAccountAuxiliary().getUnit());
                }else{
                    vo.setAccountNo(anAbstract.getAccount().getNo());
                    vo.setAccountName(anAbstract.getAccount().getName());
                }
                if(Objects.nonNull(anAbstract.getLoansMoney())){
                    vo.setCurrentPeriodLoansNum(anAbstract.getNum());
                    vo.setCurrentPeriodLoansMoney(anAbstract.getLoansMoney());
                }else if (Objects.nonNull(anAbstract.getBorrowMoney())){
                    vo.setCurrentPeriodBorrowNum(anAbstract.getNum());
                    vo.setCurrentPeriodBorrowMoney(anAbstract.getBorrowMoney());
                }else{
                    vo.setEndingBalanceNum(anAbstract.getNum());
                    vo.setEndingBalancePrice(anAbstract.getPrice());
                    vo.setEndingBalanceMoney(anAbstract.getSurplusMoney());
                }
                result.add(vo);
            }
        }

        return Result.success(result);
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
        private String accountNo;

        /**
         * 单位
         */
        private String unit;


        /**
         * 期初余额
         */

        private Long initialBalanceNum;

        private Long initialBalancePrice;



        /**
         * 本期发生额-借方
         */

        private Long currentPeriodBorrowNum;

        private Long currentPeriodBorrowMoney;


        /**
         * 本期发生额-贷方
         */

        private Long currentPeriodLoansNum;

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

        private Long endingBalanceNum;

        private Long endingBalanceMoney;

        private Long endingBalancePrice;


    }


}
