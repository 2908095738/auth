package com.bbs.converter;

import com.bbs.app.register.RegisterUser;
import com.bbs.entity.UserVO;
import com.bbs.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConverter {
    User toEntity(RegisterUser.UserRegisterParam userRegisterParam);

    @Mapping(target = "failureTokenTime", ignore = true)
    UserVO toVO(User entity);
}
