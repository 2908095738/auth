package com.bbs.financial.converter;

import com.bbs.financial.api.certificate.add.AddCertificate;
import com.bbs.financial.entity.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CertificateConverter {


    @Mapping(target = "abstracts", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "certificateWord", ignore = true)
    Certificate toEntity(AddCertificate.Param param);
}
