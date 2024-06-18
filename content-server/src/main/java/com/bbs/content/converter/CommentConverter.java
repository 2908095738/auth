package com.bbs.content.converter;

import com.bbs.content.dto.MqCommentDto;
import com.bbs.content.dto.param.CreateCommentParam;
import com.bbs.content.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentConverter {

    Comment toEntity(CreateCommentParam param);

    MqCommentDto toMqDto(CreateCommentParam param);
}
