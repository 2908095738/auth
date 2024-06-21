package com.bbs.financial.api.accountBook;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
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

    @GetMapping("/certificate/balance")
    public Result<List<Vo>> balanceAccount(@RequestParam("companyId")Long companyId,
                                           @RequestParam("createTime") String certificateCreateTime) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(companyId, certificateCreateTime, null, 1, 9999);
        List<Vo> result = new ArrayList<>();

        if(CollUtil.isNotEmpty(list.getRecords())){

            Map<Long, List<CertificateAbstract>> collect = list.getRecords().stream().collect(Collectors.groupingBy(CertificateAbstract::getAccountId));
            for (Long accountId : collect.keySet()) {
                CertificateAbstract certificateAbstract = collect.get(accountId).get(0);
                Vo vo = new Vo();
                vo.setAccountId(certificateAbstract.getAccount().getId());
                vo.setNo(certificateAbstract.getAccount().getNo());
                vo.setName(certificateAbstract.getAccount().getName());
                vo.setParentId(certificateAbstract.getAccount().getParentId());


                Long initialBalanceLoansMoney = vo.getInitialBalanceLoansMoney();//期初贷款余额
                Long initialBalanceBorrowMoney = vo.getInitialBalanceBorrowMoney();//期初借方余额
                Long currentPeriodLoansMoney = vo.getCurrentPeriodLoansMoney();//本期贷款余额
                Long currentPeriodBorrowMoney = vo.getCurrentPeriodBorrowMoney();//本期借方余额
                Long incurredYearLoansMoney = vo.getIncurredYearLoansMoney();//本年贷款余额
                Long incurredYearBorrowMoney = vo.getIncurredYearBorrowMoney();//本年借方余额
                Long endingBalanceLoansMoney = vo.getEndingBalanceLoansMoney();//期末贷款余额
                Long endingBalanceBorrowMoney = vo.getEndingBalanceBorrowMoney();//期末借方余额

                for (CertificateAbstract anAbstract : collect.get(accountId)) {


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
         * 编号
         */
        @TableField(value = "no")
        private String no;

        /**
         * 名称
         */
        @TableField(value = "name")
        private String name;

        /**
         * 上级ID
         */
        @TableField(value = "parent_id")
        private Long parentId;


        /**
         * 期初余额-借方金额
         */
        @TableField(exist = false)
        private Long initialBalanceBorrowMoney;

        /**
         * 期初余额-贷方金额
         */
        @TableField(exist = false)
        private Long initialBalanceLoansMoney;





        /**
         * 本期发生额-借方金额
         */
        @TableField(exist = false)
        private Long currentPeriodBorrowMoney;

        /**
         * 本期发生额-贷方金额
         */
        @TableField(exist = false)
        private Long currentPeriodLoansMoney;





        /**
         * 本年累计发生额-借方金额
         */
        @TableField(exist = false)
        private Long incurredYearBorrowMoney;

        /**
         * 本年累计发生额-贷方金额
         */
        @TableField(exist = false)
        private Long incurredYearLoansMoney;






        /**
         * 期末余额-借方金额
         */
        @TableField(exist = false)
        private Long endingBalanceBorrowMoney;

        /**
         * 期末余额-贷方金额
         */
        @TableField(exist = false)
        private Long endingBalanceLoansMoney;

        @TableField(exist = false)
        private List<Vo> children;

    }

}
