package com.bbs.financial.api.certificate.template;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import com.bbs.financial.entity.PriceType;
import com.bbs.financial.service.CertificateTemplateService;
import com.bbs.financial.util.LoginUser;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequestMapping
@RestController
public class SearchTemplateJoinList {

    @Resource
    private CertificateTemplateService db;

    @GetMapping("/certificate/template/join/list")
    public Result<Page<CertificateTemplate>> search(
            
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String typeNames,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(
                db.selectJoinListPage(new Page<>(current, size), CertificateTemplate.class, new MPJLambdaWrapper<CertificateTemplate>()
                        .selectAll(CertificateTemplate.class)
                        .selectCollection(CertificateTemplateAbstract.class, CertificateTemplate::getTemplateAbstractList, collection -> collection
                                .association(PriceType.class, CertificateTemplateAbstract::getPriceType)
                                .association(Account.class, CertificateTemplateAbstract::getAccount)
                        )
                        .leftJoin(CertificateTemplateAbstract.class, CertificateTemplateAbstract::getTemplateId, CertificateTemplate::getId)
                        .leftJoin(PriceType.class, PriceType::getId, CertificateTemplateAbstract::getPriceTypeId)
                        .leftJoin(Account.class, Account::getId, CertificateTemplateAbstract::getAccountId)
                        .eq(CertificateTemplate::getCompanyId, LoginUser.getCompanyId())
                        .like(isNotBlank(type), CertificateTemplate::getType, type)
                        .in(isNotBlank(typeNames), CertificateTemplate::getType, isNotBlank(typeNames) ? asList(typeNames.split(",")) : null)
                        .and(isNotBlank(name) || isNotBlank(comment), ext -> ext
                                .like(isNotBlank(name), CertificateTemplate::getName, name)
                                .or()
                                .like(isNotBlank(comment), CertificateTemplate::getComment, comment)
                        )
                )
        );
    }
}
