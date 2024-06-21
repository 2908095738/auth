package com.bbs.financial.api.accountBook;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BalanceAccount {


    @Resource
    private CertificateAbstractService certificateAbstractService;

    @GetMapping("/certificate/balance")
    public Result<List<Vo>> balanceAccount(Long companyId, String certificateCreateTime) {
        Page<CertificateAbstract> list = certificateAbstractService.selectPage(companyId, certificateCreateTime, null, 1, 9999);
        if(CollUtil.isNotEmpty(list.getRecords())){

            Map<Long, List<CertificateAbstract>> collect = list.getRecords().stream().collect(Collectors.groupingBy(CertificateAbstract::getAccountId));
            collect.keySet().forEach(accountId -> {
                CertificateAbstract certificateAbstract = collect.get(accountId).get(0);
                Vo vo = new Vo();
                vo.setAccountId(certificateAbstract.getAccount().getId());
                vo.setNo(certificateAbstract.getAccount().getNo());
                vo.setName(certificateAbstract.getAccount().getName());
                vo.setParentId(certificateAbstract.getAccount().getParentId());





//                if(certificateAbstract.getAccount() != null){
//
//                    vo.setInitialBalanceBorrowMoney();
//                    vo.setInitialBalanceLoansMoney()
//                }

            });


            for (CertificateAbstract certificateAbstract : list.getRecords()) {


            }

        }
        return Result.success(null);
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
