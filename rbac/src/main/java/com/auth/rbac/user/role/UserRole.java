package com.auth.rbac.user.role;

import com.auth.rbac.impl.dto.RoleDTO;

import java.util.List;

/**
 * 用户的角色
 * @author ext.luchenlin5
 */
public interface UserRole {

    /**
     * 通过系统编码查询角色列表（并关联查询系统信息）
     * @param systemCode 系统编码
     * @return 用户角色列表
     */
    List<RoleDTO> searchListBySystemCodeJoinSystem(String systemCode);

    /**
     * 通过系统编码查询角色列表（并关联查询系统信息）
     * @param systemCode 系统编码
     * @param userId 用户ID
     * @return 用户角色列表
     */
    List<RoleDTO> searchListBySystemCodeAndUserIdJoinSystem(String systemCode, Long userId);
}
