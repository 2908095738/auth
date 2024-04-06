package com.bbs.chat.converter;

import com.bbs.chat.dto.param.CreateThumbParam;
import com.bbs.chat.entity.Thumb;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ThumbConverter {
    Thumb toEntity(CreateThumbParam param);
}