package com.bbs.financial.api.accountBook;

import com.baomidou.mybatisplus.annotation.TableField;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.bbs.financial.util.LoginUser;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
public class QuantityController {


    @Resource
    private CertificateAbstractService certificateAbstractService;

    @GetMapping("/certificate/account/quantity")
    public Result<List<Vo>> quantityAccount(@RequestParam("startCreateTime") Date certificateStartCreateTime,
                                                             @RequestParam("endCreateTime") Date certificateEndCreateTime,
                                                             @RequestParam("cAccountId")Long accountId) {
        List<CertificateAbstract> list = certificateAbstractService.selectList(LoginUser.getCompanyId(), certificateStartCreateTime,certificateEndCreateTime, accountId);
        certificateAbstractService.initDataByMonth(list);
        return Result.success(null);
    }



    @Data
    private static class Vo {

        /**
         * 科目id
         */
        private Long accountId;

        /**
         * 日期
         */
        @TableField(value = "create_time")
        private Date createTime;

        /**
         * 凭证字
         */
        private String certificateWord;

        /**
         * 凭证编号
         */
        @TableField(value = "no")
        private Long no;

        /**
         * 科目名称
         */
        @TableField(exist = false)
        private String accountName;




        /**
         * 借方发生额-数量
         */
        @TableField(exist = false)
        private Long borrowNum;

        /**
         * 借方发生额-单价
         */
        @TableField(exist = false)
        private Long borrowPrice;

        /**
         * 借方发生额-金额
         */
        @TableField(exist = false)
        private Long borrowMoney;




        /**
         * 贷方发生额-数量
         */
        @TableField(exist = false)
        private Long loansNum;

        /**
         * 贷方发生额-单价
         */
        @TableField(exist = false)
        private Long loansPrice;

        /**
         * 贷方发生额-金额
         */
        @TableField(exist = false)
        private Long loansMoney;




        /**
         * 余额-数量
         */
        @TableField(exist = false)
        private Long surplusNum;

        /**
         * 余额-单价
         */
        @TableField(exist = false)
        private Long surplusPrice;

        /**
         * 余额-金额
         */
        @TableField(exist = false)
        private Long surplusMoney;



    }

}
