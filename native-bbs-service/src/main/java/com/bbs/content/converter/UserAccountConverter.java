package com.bbs.content.converter;


import com.bbs.content.dto.param.UpdateAccountParam;
import com.bbs.content.entity.UserAccount;
import com.bbs.entity.UserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAccountConverter {
    UserAccount toEntity(UpdateAccountParam param);

    UserAccount toEntity(UserVO currentUser);
}
