package com.bbs.auth.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.auth.entity.rbac.Role;
import com.bbs.auth.entity.System;
import com.bbs.auth.entity.rbac.UserRole;
import com.bbs.auth.mapper.RoleMapper;
import com.bbs.auth.service.RoleService;
import com.bbs.Result;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class RoleServiceImpl extends MPJBaseServiceImpl<RoleMapper, Role>
    implements RoleService {


    @Override
    public Result<Role> search(Role param) {
        return Result.success(selectJoinOne(Role.class, new MPJLambdaWrapper<Role>()
                .selectAll(Role.class)
                .leftJoin(System.class, System::getCode, Role::getSystemCode)
                .selectAssociation(System.class, Role::getSystem)
                .eq(Role::getId, param.getId())
        ));
    }

    @Override
    public Role search(Long id) {
        return selectJoinOne(Role.class, new MPJLambdaWrapper<Role>()
                .selectAll(Role.class)
                .leftJoin(System.class, System::getCode, Role::getSystemCode)
                .selectAssociation(System.class, Role::getSystem)
                .eq(Role::getId, id)
        );
    }

    @Override
    public Page<Role> searchJoinSystemPage(Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), Role.class, new MPJLambdaWrapper<Role>()
                .selectAll(Role.class)
                .leftJoin(System.class, System::getCode, Role::getSystemCode)
                .selectAssociation(System.class, Role::getSystem)
        );
    }

    @Override
    public List<Role> searchBySystemCodeJoinSystemList(String systemCode) {
        return selectJoinList(Role.class, new MPJLambdaWrapper<Role>()
                .selectAll(Role.class)
                .rightJoin(System.class, System::getCode, Role::getSystemCode)
                .selectAssociation(System.class, Role::getSystem)
                .eq(System::getCode, systemCode)
        );
    }

    @Override
    public List<Role> searchBySystemCodeAndUidJoinSystemList(String systemCode, Long userId) {
        return selectJoinList(Role.class, new MPJLambdaWrapper<Role>()
                .selectAll(Role.class)
                .rightJoin(UserRole.class, UserRole::getRoleId, Role::getId)
                .leftJoin(System.class, System::getCode, Role::getSystemCode)
                .selectAssociation(System.class, Role::getSystem)
                .eq(Role::getSystemCode, systemCode)
                .eq(UserRole::getUserId, userId)
        );
    }
}




