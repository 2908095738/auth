package com.bbs.financial.api.certificate.template;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import com.bbs.financial.entity.MoneyType;
import com.bbs.financial.service.CertificateTemplateService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.management.MemoryType;

@RequestMapping
@RestController
public class SearchTemplateJoinList {

    @Resource
    private CertificateTemplateService db;

    @GetMapping("/certificate/template/join/list")
    public Result<Page<CertificateTemplate>> search(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(
                db.selectJoinListPage(new Page<>(current, size), CertificateTemplate.class, new MPJLambdaWrapper<CertificateTemplate>()
                        .selectAll(CertificateTemplate.class)
                        .selectCollection(CertificateTemplateAbstract.class, CertificateTemplate::getTemplateAbstractList, collection -> collection
                                .association(MemoryType.class, CertificateTemplateAbstract::getMoneyType)
                        )
                        .leftJoin(CertificateTemplateAbstract.class, CertificateTemplateAbstract::getTemplateId, CertificateTemplate::getId)
                        .leftJoin(MoneyType.class, MoneyType::getId, CertificateTemplateAbstract::getMoneyTypeId)
                        .eq(CertificateTemplate::getCompanyId, companyId)
                )
        );
    }
}
