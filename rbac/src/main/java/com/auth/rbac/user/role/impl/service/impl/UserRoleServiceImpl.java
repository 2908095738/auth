package com.auth.rbac.user.role.impl.service.impl;

import com.auth.rbac.impl.dto.RoleDTO;
import com.auth.rbac.user.role.UserRole;
import com.auth.rbac.user.role.enetity.RoleEntity;
import com.auth.rbac.user.role.enetity.SystemEntity;
import com.auth.rbac.user.role.enetity.UserRoleEntity;
import com.auth.rbac.user.role.impl.converter.Converter;
import com.auth.rbac.user.role.impl.mapper.RoleMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户角色关联
 * @author ext.luchenlin5
 */
@Slf4j
@Service
public class UserRoleServiceImpl extends MPJBaseServiceImpl<RoleMapper, RoleEntity> implements UserRole {
    @Override
    public List<RoleDTO> searchListBySystemCodeJoinSystem(String systemCode) {
        List<RoleEntity> roleEntities = selectJoinList(RoleEntity.class, new MPJLambdaWrapper<RoleEntity>()
                .selectAll(RoleEntity.class)
                .rightJoin(SystemEntity.class, SystemEntity::getCode, RoleEntity::getSystemCode)
                .selectAssociation(SystemEntity.class, RoleEntity::getSystemEntity)
                .eq(SystemEntity::getCode, systemCode)
        );
        return Converter.CONVERTER.toRoleDTO(roleEntities);
    }

    @Override
    public List<RoleDTO> searchListBySystemCodeAndUserIdJoinSystem(String systemCode, Long userId) {
        List<RoleEntity> roleEntities = selectJoinList(RoleEntity.class, new MPJLambdaWrapper<RoleEntity>()
                .selectAll(RoleEntity.class)
                .rightJoin(UserRoleEntity.class, UserRoleEntity::getRoleId, RoleEntity::getId)
                .leftJoin(SystemEntity.class, SystemEntity::getCode, RoleEntity::getSystemCode)
                .selectAssociation(System.class, RoleEntity::getSystemEntity)
                .eq(RoleEntity::getSystemCode, systemCode)
                .eq(UserRoleEntity::getUserId, userId)
        );
        return Converter.CONVERTER.toRoleDTO(roleEntities);
    }
}
