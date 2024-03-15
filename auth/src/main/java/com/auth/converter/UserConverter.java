package com.auth.converter;

import com.auth.app.register.RegisterUser;
import com.auth.entity.UserVO;
import com.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConverter {
    User toEntity(RegisterUser.UserRegisterParam userRegisterParam);

    @Mapping(target = "failureTokenTime", ignore = true)
    UserVO toVO(User entity);
}
