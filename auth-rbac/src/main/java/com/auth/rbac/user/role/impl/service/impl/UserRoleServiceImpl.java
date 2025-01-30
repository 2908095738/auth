package com.auth.rbac.user.role.impl.service.impl;

import com.auth.rbac.user.role.RBAC;
import com.auth.rbac.user.role.enetity.UserRoleEntity;
import com.auth.rbac.user.role.impl.mapper.UserRoleMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联
 * @author ext.luchenlin5
 */
@Slf4j
@Primary
@Service
public class UserRoleServiceImpl extends MPJBaseServiceImpl<UserRoleMapper, UserRoleEntity> implements RBAC.UseRole {

    @Override
    public Boolean remove(String systemCode, Long userId) {
        return lambdaUpdate().eq(UserRoleEntity::getSystemCode, systemCode).eq(UserRoleEntity::getUserId, userId).remove();
    }

    @Transactional
    @Override
    public Boolean add(String systemCode, Long userId, List<Long> roleIds) {
        List<UserRoleEntity> param = converterParam(systemCode, userId, roleIds);
        return remove(systemCode, userId) && saveBatch(param);
    }

    private List<UserRoleEntity> converterParam(String systemCode, Long userId, List<Long> roleIds) {
        return roleIds.stream().map(roleId -> new UserRoleEntity(userId, systemCode, roleId)).collect(Collectors.toList());
    }
}
