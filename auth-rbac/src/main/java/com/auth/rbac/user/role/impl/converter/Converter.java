package com.auth.rbac.user.role.impl.converter;

import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.dto.RoleDTO;
import com.auth.rbac.dto.SystemDTO;
import com.auth.rbac.user.role.enetity.MenuEntity;
import com.auth.rbac.user.role.enetity.RoleEntity;
import com.auth.rbac.user.role.enetity.SystemEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 对象转换
 * @author ext.luchenlin5
 */
@Mapper(componentModel = "spring")
public interface Converter {

    List<RoleDTO> toRoleDTO(List<RoleEntity> entity);

    Page<RoleDTO> toRoleDTO(Page<RoleEntity> entity);

    RoleDTO toRoleDTO(RoleEntity entity);

    MenuEntity toMenuEntity(MenuDTO dto);

    MenuDTO toMenuDTO(MenuEntity entity);

    List<MenuDTO> toMenuDTO(List<MenuEntity> entityList);

    SystemEntity toEntity(SystemDTO dto);

    SystemDTO toSystemDTO(SystemEntity dto);

    Page<SystemDTO> toSystemDTOPage(Page<SystemEntity> entityPage);

    List<SystemDTO> toSystemDTOList(List<SystemEntity> entityList);
}
