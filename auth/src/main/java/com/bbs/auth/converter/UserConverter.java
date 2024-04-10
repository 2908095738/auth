package com.bbs.auth.converter;

import com.bbs.auth.app.register.Register;
import com.bbs.auth.app.search.SearchUser;
import com.bbs.entity.UserVO;
import com.bbs.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(source = "userName", target = "name")
    User toEntity(Register.Param param);

    @Mapping(target = "failureTokenTime", ignore = true)
    UserVO toVO(User entity);

    SearchUser.VO toSearchUserVO(User entity);
}
