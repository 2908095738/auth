package com.bbs.auth.service.impl;


import com.bbs.auth.controller.RoleController;
import com.bbs.auth.entity.RoleGroup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.auth.entity.Role;
import com.bbs.auth.entity.System;
import com.bbs.auth.mapper.RoleMapper;
import com.bbs.auth.service.RoleService;
import com.bbs.Result;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

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
    public Page<Role> searchJoinSystem(Integer current, Integer size) {
        return selectJoinListPage(new Page<>(current, size), Role.class, new MPJLambdaWrapper<Role>()
                .selectAll(Role.class)
                .leftJoin(System.class, System::getCode, Role::getSystemCode)
                .selectAssociation(System.class, Role::getSystem)
        );
    }
}




