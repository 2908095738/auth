package com.auth.user.impl.converter;

import com.auth.user.dto.UserDTO;
import com.auth.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户信息转换
 * @author ext.luchenlin5
 */
@Mapper
public interface UserConverter {

    UserConverter CONVERTER = Mappers.getMapper(UserConverter.class);

    UserDTO toDTO(UserEntity entity);

    List<UserDTO> toDTO(List<UserEntity> entity);

    UserEntity toEntity(UserDTO userDTO);
}
