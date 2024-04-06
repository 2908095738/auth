package com.bbs.chat.converter;

import com.bbs.chat.dto.MqFollowDto;
import com.bbs.chat.entity.Fan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowConverter {
    Fan toEntity(MqFollowDto param);
}