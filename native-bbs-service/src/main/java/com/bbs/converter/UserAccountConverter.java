package com.bbs.converter;

import com.bbs.dto.param.UpdateAccountParam;
import com.bbs.entity.UserAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAccountConverter {
    UserAccount toEntity(UpdateAccountParam param);
}
