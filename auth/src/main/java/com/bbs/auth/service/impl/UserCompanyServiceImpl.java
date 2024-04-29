package com.bbs.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.auth.entity.UserCompany;
import com.bbs.auth.service.UserCompanyService;
import com.bbs.auth.mapper.UserCompanyMapper;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【user_company(用户 & 公司关系表)】的数据库操作Service实现
* @createDate 2024-04-28 11:36:34
*/
@Service
public class UserCompanyServiceImpl extends ServiceImpl<UserCompanyMapper, UserCompany>
    implements UserCompanyService{

}




