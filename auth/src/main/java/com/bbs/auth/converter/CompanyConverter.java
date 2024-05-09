package com.bbs.auth.converter;

import com.bbs.auth.app.company.RegisterCompany;
import com.bbs.auth.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyConverter {

    Company toEntity(RegisterCompany.Param param);
}
