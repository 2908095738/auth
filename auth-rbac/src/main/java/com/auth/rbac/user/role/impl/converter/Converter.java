package com.auth.rbac.user.role.impl.converter;

import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.dto.RoleDTO;
import com.auth.rbac.dto.SystemDTO;
import com.auth.rbac.user.role.enetity.MenuEntity;
import com.auth.rbac.user.role.enetity.RoleEntity;
import com.auth.rbac.user.role.enetity.SystemEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 对象转换
 * @author ext.luchenlin5
 */
@Mapper(componentModel = "spring")
public interface Converter {

    Converter CONVERTER = Mappers.getMapper(Converter.class);

    List<RoleDTO> toRoleDTO(List<RoleEntity> entity);

    RoleDTO toRoleDTO(RoleEntity entity);

    List<MenuDTO> toMenuDTO(List<MenuEntity> entityList);

    SystemEntity toEntity(SystemDTO dto);

    Page<SystemDTO> toSystemDTOPage(Page<SystemEntity> entityPage);
}
