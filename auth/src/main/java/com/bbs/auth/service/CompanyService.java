package com.bbs.auth.service;

import com.bbs.auth.entity.Company;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 路晨霖
* @description 针对表【company(公司)】的数据库操作Service
* @createDate 2024-04-28 11:36:25
*/
public interface CompanyService extends IService<Company> {

    Boolean exists(Company company);

    Boolean notExists(Company company);
}
