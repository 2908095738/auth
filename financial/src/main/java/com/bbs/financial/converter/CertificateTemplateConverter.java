package com.bbs.financial.converter;

import com.bbs.financial.api.certificate.template.AddTemplate;
import com.bbs.financial.entity.CertificateTemplate;
import com.bbs.financial.entity.CertificateTemplateAbstract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CertificateTemplateConverter {

    CertificateTemplate toEntity(AddTemplate.Param param);

    @Mapping(target = "borrowMoney", ignore = true)
    @Mapping(target = "loansMoney", ignore = true)
    CertificateTemplateAbstract toEntity(AddTemplate.TemplateAbstract templateAbstract);
}
