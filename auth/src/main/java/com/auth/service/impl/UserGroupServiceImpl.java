package com.auth.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth.entity.UserGroup;
import com.auth.mapper.UserGroupMapper;
import com.auth.service.UserGroupService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup>
    implements UserGroupService {

}




