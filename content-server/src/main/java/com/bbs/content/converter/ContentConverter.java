package com.bbs.content.converter;

import com.bbs.content.api.content.ReleaseContent;
import com.bbs.content.entity.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContentConverter {

    @Mapping(target = "cover", ignore = true)
    Article toEntity(ReleaseContent.Param param);
}
