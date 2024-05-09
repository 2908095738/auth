package com.bbs.stream.converter;

import com.bbs.stream.dto.param.CreateStreamParam;
import com.bbs.stream.entity.Stream;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StreamConverter {
    //TODO 忽略字段之后再看
    Stream toEntity(CreateStreamParam param);
}