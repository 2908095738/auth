package com.bbs.auth.service;

import com.bbs.auth.entity.Company;
import com.github.yulichang.base.MPJBaseService;

/**
* @author 路晨霖
* @description 针对表【company(公司)】的数据库操作Service
* @createDate 2024-04-28 11:36:25
*/
public interface CompanyService extends MPJBaseService<Company> {

    Boolean exists(Company company);

    Boolean notExists(Company company);
}
