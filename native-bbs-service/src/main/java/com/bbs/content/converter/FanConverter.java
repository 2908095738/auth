package com.bbs.content.converter;

import com.bbs.content.dto.param.CreateFollowParam;
import com.bbs.content.dto.param.DelFollowParam;
import com.bbs.content.entity.Fan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FanConverter {
    Fan toEntity(CreateFollowParam param);
    Fan toEntity(DelFollowParam param);
}
