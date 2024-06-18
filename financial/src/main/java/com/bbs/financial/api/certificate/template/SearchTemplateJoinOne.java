package com.bbs.financial.api.certificate.template;

import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import com.bbs.financial.entity.PriceType;
import com.bbs.financial.service.CertificateTemplateService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping
@RestController
public class SearchTemplateJoinOne {

    @Resource
    private CertificateTemplateService db;

    @GetMapping("/certificate/template/join")
    public Result<CertificateTemplate> search(
            @RequestParam Long id
    ) {
        return Result.success(
                db.selectJoinOne(CertificateTemplate.class, new MPJLambdaWrapper<CertificateTemplate>()
                        .selectAll(CertificateTemplate.class)
                        .selectCollection(CertificateTemplateAbstract.class, CertificateTemplate::getTemplateAbstractList, collection -> collection
                                .association(PriceType.class, CertificateTemplateAbstract::getPriceType)
                                .association(Account.class, CertificateTemplateAbstract::getAccount)
                        )
                        .leftJoin(CertificateTemplateAbstract.class, CertificateTemplateAbstract::getTemplateId, CertificateTemplate::getId)
                        .leftJoin(PriceType.class, PriceType::getId, CertificateTemplateAbstract::getPriceTypeId)
                        .leftJoin(Account.class, Account::getId, CertificateTemplateAbstract::getAccountId)
                        .eq(CertificateTemplate::getId, id)
                )
        );
    }
}
