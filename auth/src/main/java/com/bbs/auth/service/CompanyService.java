package com.bbs.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.auth.entity.Company;
import com.bbs.auth.entity.CompanyStructure;
import com.bbs.auth.entity.UserCompany;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
* @author 路晨霖
* @description 针对表【company(公司)】的数据库操作Service
* @createDate 2024-04-28 11:36:25
*/
public interface CompanyService extends MPJBaseService<Company> {

    Boolean exists(Company company);

    Boolean notExists(Company company);

    List<CompanyStructure> searchStructure(Long companyID);

    List<CompanyStructure> searchStructure();

    Company searchCompanyStaff(Long companyID, Page<UserCompany> page);

    List<UserCompany> searchCompany(Long uid);
}
