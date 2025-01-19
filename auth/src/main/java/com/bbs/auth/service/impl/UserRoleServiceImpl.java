package com.bbs.auth.service.impl;

import com.bbs.auth.entity.rbac.UserRole;
import com.bbs.auth.mapper.UserRoleMapper;
import com.bbs.auth.service.UserRoleService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author ext.luchenlin5
 */
@Service
public class UserRoleServiceImpl extends MPJBaseServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
