package com.bbs.financial.api.report.flows;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.AccountService;
import com.bbs.financial.service.CertificateService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
@RequestMapping
public class FlowsReport {

    @Resource
    private CertificateService certificateService;
    @Resource
    private AccountService accountService;


    @GetMapping("/report/flows")
    public Result<Boolean> count(
            @RequestParam(value = "startDate") Long startDateLong,
            @RequestParam(value = "endDate") Long endDateLong
    ) {
        Date startDate = getStartDate(startDateLong);
        Date endDate = getEndDate(endDateLong);
        // 1.6 收到其他与经营活动有关的现金
        List<Long> cashInBankAccountIds = accountService.listObjs(new LambdaQueryWrapper<Account>().select(Account::getId)
                .in(Account::getNo, 6602)
                .notIn(Account::getId, 901, 1051, 1057)
        );
        List<Certificate> certificates = certificateService.selectJoinList(Certificate.class, baseWrapper(startDate, endDate)
                .in(CertificateAbstract::getAccountId, cashInBankAccountIds)   //筛选【银行存款】【其他货币资金】科目相关凭证
        );
        long cashInBankLoansMoneySum = computeLoansMoneySum(certificates);

        // 1.4 支付的职工薪酬
        long endowmentInsuranceLoansMoneySum = computeLoansMoneySum(certificateService.selectJoinList(Certificate.class, baseWrapper(startDate, endDate)
                .eq(Account::getId, 1051)   //筛选【员工养老保险】科目相关凭证
        ));


        // 查询指定日期范围内，包含【银行存款】、【其他货币资金】科目的凭证
//        long cashInBankLoansMoneySum = certificateService.searchByCreate(startDate, endDate, String.valueOf(1002)).stream()
//                .map(Certificate::getAbstracts).flatMap(Collection::stream)
//                .mapToLong(CertificateAbstract::getLoansMoney).sum();
        long sum = certificateService.searchByCreate(startDate, endDate, String.valueOf(1002)).stream()
                .map(Certificate::getAbstracts).flatMap(Collection::stream)
                .mapToLong(CertificateAbstract::getLoansMoney).sum();
        return Result.success();
    }

    private MPJLambdaWrapper<Certificate> baseWrapper(Date startDate, Date endDate) {
        return new MPJLambdaWrapper<Certificate>()
                .selectAll(Certificate.class)
                .leftJoin(CertificateAbstract.class, CertificateAbstract::getCertificateId, Certificate::getId, ext -> ext
                        .selectAssociation(CertificateAbstract.class, Certificate::getAbstracts)
                )
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId)
                .ge(Certificate::getCreateTime, startDate)
                .lt(Certificate::getCreateTime, endDate);
    }

    private long computeLoansMoneySum(List<Certificate> certificates) {
        return certificates.stream()
                .map(Certificate::getAbstracts).flatMap(Collection::stream)
                .mapToLong(CertificateAbstract::getLoansMoney).sum();
    }

    private Date getStartDate(Long startDateLong) {
        Date now = new Date();
        return nonNull(startDateLong) ? new Date(startDateLong) : DateUtil.beginOfMonth(now);
    }

    private Date getEndDate(Long endDateLong) {
        Date now = new Date();
        return nonNull(endDateLong) ? new Date(endDateLong) : DateUtil.offsetMonth(now, INTEGER_ONE);
    }
}
