package com.auth.user.impl.converter;

import com.auth.user.dto.UserDTO;
import com.auth.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户信息转换
 * @author ext.luchenlin5
 */
@Mapper
public interface UserConverter {

    UserConverter CONVERTER = Mappers.getMapper(UserConverter.class);

    UserDTO toDTO(User entity);
}
