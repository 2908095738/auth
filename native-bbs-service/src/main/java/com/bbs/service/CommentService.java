package com.bbs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface CommentService extends IService<Comment> {

    Page<GetUserNewsDto.CommentByNewIdDto> getPageByNewId(Long newId, Integer current, Integer size);
}
