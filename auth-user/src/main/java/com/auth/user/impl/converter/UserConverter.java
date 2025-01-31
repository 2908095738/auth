package com.auth.user.impl.converter;

import com.auth.user.dto.UserDTO;
import com.auth.user.entity.UserEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 用户信息转换
 * @author ext.luchenlin5
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserDTO toDTO(UserEntity entity);

    Page<UserDTO> toDTO(Page<UserEntity> entity);

    List<UserDTO> toDTO(List<UserEntity> entity);

    UserEntity toEntity(UserDTO userDTO);
}
