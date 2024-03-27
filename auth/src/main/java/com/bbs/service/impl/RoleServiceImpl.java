package com.bbs.service.impl;


import com.bbs.controller.RoleController;
import com.bbs.entity.RoleGroup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.entity.Role;
import com.bbs.mapper.RoleMapper;
import com.bbs.service.RoleService;
import com.clinic.Result;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

/**
 *
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService {

    @Override
    public Result<Page<Role>> search(RoleController.QueryRoleParam param) {
        if(isNull(param.getGroupId())) {
            return Result.success(page(param.toPage()));
        } else {
            return Result.success(baseMapper.selectJoinPage(param.toPage(), Role.class, new MPJLambdaWrapper<>(Role.class)
                    .rightJoin(RoleGroup.class, RoleGroup::getGroupId, Role::getId, ext  -> ext
                            .selectAll(Role.class)
                    )
                    .eq(RoleGroup::getGroupId, param.getGroupId())
            ));
        }
    }
}




