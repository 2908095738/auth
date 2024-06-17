package com.bbs.financial.api.certificate.count;

import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.CertificateAbstract;
import com.bbs.financial.service.CertificateAbstractService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class CountCertificate {

    @Resource
    private CertificateAbstractService abstractService;

    @GetMapping("/certificate/count")
    public Result<List<CertificateAbstract>> count() {
        return Result.success(abstractService.selectJoinList(CertificateAbstract.class, new MPJLambdaWrapper<CertificateAbstract>()
                .selectSum(CertificateAbstract::getBorrowMoney)
                .selectSum(CertificateAbstract::getLoansMoney)
                .leftJoin(Account.class, Account::getId, CertificateAbstract::getAccountId, ext -> ext
                        .selectAssociation(Account.class, CertificateAbstract::getAccount)
                )
                .isNotNull(CertificateAbstract::getAccountId)
                .groupBy(CertificateAbstract::getAccountId)
        ));
    }
}
