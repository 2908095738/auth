package com.auth.service.impl;

import com.auth.entity.UserBind;
import com.auth.service.UserBindService;
import com.auth.mapper.UserBindMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【user_bind(用户绑定表)】的数据库操作Service实现
* @createDate 2024-03-19 21:48:13
*/
@Service
public class UserBindServiceImpl extends MPJBaseServiceImpl<UserBindMapper, UserBind>
    implements UserBindService{

}




