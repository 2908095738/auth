package com.bbs.chat.converter;

import com.bbs.chat.dto.MqCommentDto;
import com.bbs.chat.dto.param.CreateCommentParam;
import com.bbs.chat.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentConverter {

    Comment toEntity(MqCommentDto param);
}