package com.bbs.financial.api.accountBook;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.enums.BorrowOrLoansType;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BalanceAccount {


    @Resource
    private CertificateAbstractService certificateAbstractService;

    @GetMapping("/certificate/account/balance")
    public Result<List<Vo>> balanceAccount(@RequestParam("createTime") String certificateCreateTime) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(LoginUser.getCompanyId(), certificateCreateTime, null, 1, 9999);
        List<Vo> result = new ArrayList<>();

        if(CollUtil.isNotEmpty(list.getRecords())){
            certificateAbstractService.initDataByMonth(list.getRecords());
            Map<Long, List<CertificateAbstract>> collect = list.getRecords().stream().collect(Collectors.groupingBy(CertificateAbstract::getAccountId));
            for (Long accountId : collect.keySet()) {
                CertificateAbstract certificateAbstract = collect.get(accountId).get(0);
                Vo vo = new Vo();
                vo.setAccountId(certificateAbstract.getAccount().getId());
                vo.setNo(certificateAbstract.getAccount().getNo());
                vo.setName(certificateAbstract.getAccount().getName());
                vo.setParentId(certificateAbstract.getAccount().getParentId());


                Long initialBalanceLoansMoney = 0L;//期初贷款余额
                Long initialBalanceBorrowMoney = 0L;//期初借方余额
                Long currentPeriodLoansMoney = 0L;//本期贷款余额
                Long currentPeriodBorrowMoney = 0L;//本期借方余额
                Long incurredYearLoansMoney = 0L;//本年贷款余额
                Long incurredYearBorrowMoney = 0L;//本年借方余额

                Long endingBalanceLoansMoney = 0L;//期末贷款余额
                Long endingBalanceBorrowMoney = 0L;//期末借方余额

                for (CertificateAbstract anAbstract : collect.get(accountId)) {
                    if(StrUtil.isNotBlank(anAbstract.getCertificateAbstract())){
                        if(anAbstract.getCertificateAbstract().equals("期初余额")){
                            initialBalanceLoansMoney=anAbstract.getLoansMoney();
                            initialBalanceBorrowMoney=anAbstract.getBorrowMoney();
                        }
                        if(anAbstract.getCertificateAbstract().equals("本期合计")){
                            currentPeriodLoansMoney=anAbstract.getLoansMoney();
                            currentPeriodBorrowMoney=anAbstract.getBorrowMoney();
                            //期末
                            if(ObjUtil.isNotEmpty(anAbstract.getAccount())){
                                if(StrUtil.isBlank(anAbstract.getAccount().getDirection())||anAbstract.getAccount().getDirection().equals(BorrowOrLoansType.BORROW.getValue())){//借
                                    endingBalanceLoansMoney=anAbstract.getLoansMoney()-anAbstract.getBorrowMoney();
                                }else{//贷
                                    endingBalanceBorrowMoney=anAbstract.getBorrowMoney()-anAbstract.getLoansMoney();
                                }
                            }
                        }
                        if(anAbstract.getCertificateAbstract().equals("本年累计")){
                            incurredYearLoansMoney=anAbstract.getLoansMoney();
                            incurredYearBorrowMoney=anAbstract.getBorrowMoney();
                        }
                    }
                }
                vo.setInitialBalanceLoansMoney(initialBalanceLoansMoney);
                vo.setInitialBalanceBorrowMoney(initialBalanceBorrowMoney);
                vo.setCurrentPeriodLoansMoney(currentPeriodLoansMoney);
                vo.setCurrentPeriodBorrowMoney(currentPeriodBorrowMoney);
                vo.setIncurredYearLoansMoney(incurredYearLoansMoney);
                vo.setIncurredYearBorrowMoney(incurredYearBorrowMoney);
                vo.setEndingBalanceLoansMoney(endingBalanceLoansMoney);
                vo.setEndingBalanceBorrowMoney(endingBalanceBorrowMoney);
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
         * 编号
         */
        private String no;

        /**
         * 名称
         */
        private String name;

        /**
         * 上级ID
         */
        private Long parentId;


        /**
         * 期初余额-借方金额
         */
        private Long initialBalanceBorrowMoney;

        /**
         * 期初余额-贷方金额
         */
        private Long initialBalanceLoansMoney;





        /**
         * 本期发生额-借方金额
         */
        private Long currentPeriodBorrowMoney;

        /**
         * 本期发生额-贷方金额
         */
        private Long currentPeriodLoansMoney;





        /**
         * 本年累计发生额-借方金额
         */
        private Long incurredYearBorrowMoney;

        /**
         * 本年累计发生额-贷方金额
         */
        private Long incurredYearLoansMoney;






        /**
         * 期末余额-借方金额
         */
        private Long endingBalanceBorrowMoney;

        /**
         * 期末余额-贷方金额
         */
        private Long endingBalanceLoansMoney;


        private List<Vo> children;

    }

}
