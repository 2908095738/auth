package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.cache.ThumbCache;
import com.bbs.dto.GetUserNewsDto;
import com.bbs.entity.Comment;
import com.bbs.enums.NewCommentStatus;
import com.bbs.mapper.CommentMapper;
import com.bbs.service.CommentService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 *
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

    private ThumbCache thumbCache;

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
        Page<GetUserNewsDto.CommentByNewIdDto> result = wrapper.selectAll(Comment.class)
                .eq(Comment::getStatus, NewCommentStatus.HAVE_RELEASED.getCode())
                .page(new Page<>(current, size));
        List<GetUserNewsDto.CommentByNewIdDto> resultRecords = result.getRecords();
        if(isNotEmpty(resultRecords)) {
            List<Long> commentIds = resultRecords.stream().map(GetUserNewsDto.CommentByNewIdDto::getId).collect(Collectors.toList());
            Integer count = thumbCache.countBy(null, null, commentIds, 3);
            result.getRecords().forEach(o -> o.setLikeCount(count));
        }
        return result;
    }
}




