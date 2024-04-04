package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.entity.Comment;

/**
 *
 */
public interface CommentService extends IService<Comment> {

    Page<GetUserNewsDto.CommentByNewIdDto> getPageByNewId(Long newId, Integer current, Integer size);

    Boolean delById(Long commentId);

}
