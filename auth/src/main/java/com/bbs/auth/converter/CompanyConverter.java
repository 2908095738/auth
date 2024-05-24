package com.bbs.auth.converter;

import com.bbs.auth.app.company.RegisterCompany;
import com.bbs.auth.entity.Company;
import com.bbs.vo.CompanyStructure;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyConverter {

    Company toEntity(RegisterCompany.Param param);

    List<com.bbs.vo.Company> toVO(List<Company> entity);

    List<CompanyStructure> toCSVO(List<com.bbs.auth.entity.CompanyStructure> companyStructures);
}
