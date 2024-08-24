package com.bbs.financial.converter;

import com.bbs.financial.api.certificate.add.AddCertificate;
import com.bbs.financial.entity.Certificate;
import com.bbs.financial.enums.CertTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CertificateConverter {


    @Mapping(target = "abstracts", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "certificateWord", ignore = true)

    @Mapping(source = "type", target = "type", qualifiedByName = "getCertType")
    Certificate toEntity(AddCertificate.Param param);

    @Named("getCertType")
    default CertTypeEnum getCertType(Integer type) {
        return CertTypeEnum.enumMap.get(type);
    }
}
