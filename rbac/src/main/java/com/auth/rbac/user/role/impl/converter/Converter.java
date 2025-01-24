package com.auth.rbac.user.role.impl.converter;

import com.auth.rbac.impl.dto.RoleDTO;
import com.auth.rbac.user.role.enetity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 对象转换
 * @author ext.luchenlin5
 */
@Mapper
public interface Converter {

    Converter CONVERTER = Mappers.getMapper(Converter.class);

    List<RoleDTO> toRoleDTO(List<RoleEntity> entity);
}
