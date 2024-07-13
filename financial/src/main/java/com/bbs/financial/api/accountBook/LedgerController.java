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
public class LedgerController {

    @Resource
    private CertificateAbstractService certificateAbstractService;


    @GetMapping("/certificate/account/ledger")
    public Result<List<Vo>> ledgerAccount(@RequestParam("startCreateTime") Date certificateStartCreateTime,
                                                             @RequestParam("endCreateTime") Date certificateEndCreateTime,
                                                             @RequestParam("cAccountId")Long accountId) {
        List<CertificateAbstract> list = certificateAbstractService.selectList(LoginUser.getCompanyId(), certificateStartCreateTime,certificateEndCreateTime, accountId);
        certificateAbstractService.initDataByMonth(list);
        return Result.success(null);
    }


    @Data
    private static class Vo {

        /**
         * 科目名称
         */
        @TableField(exist = false)
        private String accountName;


        /**
         * 科目编号
         */
        private Long accountNo;

        /**
         * 单位
         */
        @TableField(exist = false)
        private String unit;




    }


}
