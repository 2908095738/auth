package com.bbs.converter;

import com.bbs.dto.param.CreateCommentParam;
import com.bbs.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentConverter {

    Comment toEntity(CreateCommentParam param);
}
