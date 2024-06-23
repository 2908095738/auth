package com.bbs.financial.api.certificate.count;

import cn.hutool.core.date.DateUtil;
import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@RestController
@RequestMapping
public class CountCertificate {

    @Resource
    private CertificateAbstractService abstractService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Param {

        private Long startDate;

        private Long endDate;
    }

    @GetMapping("/certificate/count")
    public Result<List<CertificateAbstract>> count(Param param) {
        return Result.success(abstractService.selectJoinList(CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectSum(CertificateAbstract::getBorrowMoney)
                .selectSum(CertificateAbstract::getLoansMoney)
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId, ext -> ext
                        .selectAssociation(Account.class, CertificateAbstract::getAccount)
                )
                .leftJoin(Certificate.class, Certificate::getId, CertificateAbstract::getCertificateId)
                .or(nonNull(param.startDate), wrapper -> wrapper
                        .ge(Certificate::getCreateTime, nonNull(param.startDate) ? DateUtil.beginOfMonth(new Date(param.startDate)) : null)
                        // 最大时间使用传入的 endDate 取当月最后一天（如果只查单月，则 endDate 可空，并使用传入的 startDate 替换计算最后一天）
                        .lt(Certificate::getCreateTime, nonNull(param.endDate) ? DateUtil.offsetMonth(new Date(param.endDate), INTEGER_ONE) : DateUtil.offsetMonth(new Date(param.startDate), INTEGER_ONE))
                )
                .isNotNull(CertificateAbstract::getAccountId)
                .groupBy(CertificateAbstract::getAccountId)
        ));
    }
}
