package com.bbs.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.chat.entity.Comment;

public interface CommentService extends IService<Comment> {
    Result createComment(Comment comment);
}