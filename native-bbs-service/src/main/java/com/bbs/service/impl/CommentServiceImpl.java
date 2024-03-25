package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.entity.Comment;
import com.bbs.entity.News;
import com.bbs.entity.Thumb;
import com.bbs.mapper.CommentMapper;
import com.bbs.service.CommentService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    /**
     * 查询评论、点赞
     * @param newId
     * @param current
     * @param size
     * @return
     */
    @Override
    public Page<GetUserNewsDto.CommentByNewIdDto> getPageByNewId(Long newId, Integer current, Integer size) {
        MPJLambdaWrapper<GetUserNewsDto.CommentByNewIdDto> wrapper = new MPJLambdaWrapper<>();
        return wrapper.selectAll(News.class)
                .selectCount(Thumb::getId, Comment::getLikeCount)
                .leftJoin(Thumb.class, Thumb::getTcId, Comment::getNewId)
                .page(new Page<>(current,size));
    }
}




