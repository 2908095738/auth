package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface CommentService extends IService<Comment> {

    Page<GetUserNewsDto.CommentByNewIdDto> getPageByNewId(Long newId, Integer current, Integer size);

    Result<Boolean> delById(Long commentId);

}
