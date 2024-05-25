package com.bbs.financial.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.bbs.financial.entity.Account;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import com.bbs.financial.entity.PriceType;
import com.bbs.financial.mapper.CertificateTemplateMapper;
import com.bbs.financial.service.CertificateTemplateService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 路晨霖
* @description 针对表【certificate_template(记账凭证：模板)】的数据库操作Service实现
* @createDate 2024-05-23 03:05:38
*/
@Service
public class CertificateTemplateServiceImpl extends MPJBaseServiceImpl<CertificateTemplateMapper, CertificateTemplate>
    implements CertificateTemplateService{

    @Override
    public List<CertificateTemplate> getJoinTemplate(Long companyId, List<String> templateNames) {
        return selectJoinList(CertificateTemplate.class, new MPJLambdaWrapper<CertificateTemplate>()
                .selectAll(CertificateTemplate.class)
                .selectCollection(CertificateTemplateAbstract.class, CertificateTemplate::getTemplateAbstractList, collection -> collection
                        .association(PriceType.class, CertificateTemplateAbstract::getPriceType)
                        .association(Account.class, CertificateTemplateAbstract::getAccount)
                )
                .leftJoin(CertificateTemplateAbstract.class, CertificateTemplateAbstract::getTemplateId, CertificateTemplate::getId)
                .leftJoin(PriceType.class, PriceType::getId, CertificateTemplateAbstract::getPriceTypeId)
                .leftJoin(Account.class, Account::getId, CertificateTemplateAbstract::getAccountId)
                .eq(CertificateTemplate::getIsActive, 1)
                .eq(CertificateTemplate::getCompanyId, companyId)
                .in(CollUtil.isNotEmpty(templateNames), CertificateTemplate::getType, templateNames)
        );
    }
}




