package com.bbs.auth.converter;

import com.bbs.auth.app.follow.DelFollow;
import com.bbs.auth.app.follow.Follow;
import com.bbs.auth.entity.Fan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FanConverter {
    Fan toEntity(Follow.Param param);
    Fan toEntity(DelFollow.Param param);
}
