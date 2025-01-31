package com.auth.rbac.user.role.impl.service.impl;

import com.auth.rbac.dto.RoleDTO;
import com.auth.rbac.dto.SystemDTO;
import com.auth.rbac.user.role.RBAC;
import com.auth.rbac.user.role.enetity.RoleEntity;
import com.auth.rbac.user.role.enetity.SystemEntity;
import com.auth.rbac.user.role.enetity.UserRoleEntity;
import com.auth.rbac.user.role.impl.converter.Converter;
import com.auth.rbac.user.role.impl.mapper.RoleMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Service
public class RoleServiceImpl extends MPJBaseServiceImpl<RoleMapper, RoleEntity> implements RBAC.Role {
    
    @Resource
    private Converter converter;

    @Resource
    private RBAC.System rbacSystem;

    @Override
    public List<RoleDTO> searchList(String systemCode) {
        List<RoleEntity> roleEntities = selectJoinList(RoleEntity.class, new MPJLambdaWrapper<RoleEntity>()
                .selectAll(RoleEntity.class)
                .rightJoin(SystemEntity.class, SystemEntity::getCode, RoleEntity::getSystemCode)
                .selectAssociation(SystemEntity.class, RoleEntity::getSystemEntity)
                .eq(SystemEntity::getCode, systemCode)
        );
        return converter.toRoleDTO(roleEntities);
    }

    @Override
    public List<RoleDTO> searchList(String systemCode, Long userId) {
        List<RoleEntity> roleEntities = selectJoinList(RoleEntity.class, new MPJLambdaWrapper<RoleEntity>()
                .selectAll(RoleEntity.class)
                .rightJoin(UserRoleEntity.class, UserRoleEntity::getRoleId, RoleEntity::getId)
                .leftJoin(SystemEntity.class, SystemEntity::getCode, RoleEntity::getSystemCode)
                .selectAssociation(SystemEntity.class, RoleEntity::getSystemEntity)
                .eq(RoleEntity::getSystemCode, systemCode)
                .eq(UserRoleEntity::getUserId, userId)
        );
        return converter.toRoleDTO(roleEntities);
    }

    @Override
    public RoleDTO searchById(Long id) {
        return converter.toRoleDTO(getById(id));
    }

    @Override
    public Page<RoleDTO> page(Integer current, Integer size) {
        Page<RoleEntity> page = super.page(new Page<>(current, size));
        Page<RoleDTO> result = converter.toRoleDTO(page);
        List<RoleDTO> records = result.getRecords();
        Set<String> systemCodeSet = records.stream().map(RoleDTO::getSystemCode).collect(Collectors.toSet());
        Map<String, SystemDTO> systemCodeMapping = rbacSystem.searchBySystemCode(systemCodeSet);
        records.forEach(role -> {
            String systemCode = role.getSystemCode();
            if(StringUtils.isNotBlank(systemCode)) {
                role.setSystemDTO(systemCodeMapping.get(systemCode));
            }
        });
        return result;
    }
}
