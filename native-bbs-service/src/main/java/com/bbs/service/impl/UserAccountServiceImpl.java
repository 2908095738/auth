package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.entity.UserAccount;
import com.bbs.service.UserAccountService;
import com.bbs.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount>
    implements UserAccountService{

}




