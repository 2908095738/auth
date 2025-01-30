package com.auth.rbac.user.role.impl.service.impl;

import com.auth.rbac.user.role.RBAC;
import com.auth.rbac.user.role.enetity.RoleMenuEntity;
import com.auth.rbac.user.role.impl.mapper.RoleMenuMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Service
public class RoleMenuServiceImpl extends MPJBaseServiceImpl<RoleMenuMapper, RoleMenuEntity> implements RBAC.RoleMenu {
    @Override
    public Boolean removeByRoleId(Long roleId) {
        return lambdaUpdate().eq(RoleMenuEntity::getRoleId, roleId).remove();
    }

    @Override
    public Boolean save(Long roleId, List<Long> menuIds) {
        List<RoleMenuEntity> params = converter(roleId, menuIds);
        return saveBatch(params);
    }

    public List<RoleMenuEntity> converter(Long roleId, List<Long> menuIds) {
        return menuIds.stream().map(menuId -> new RoleMenuEntity(roleId, menuId)).collect(Collectors.toList());
    }
}
