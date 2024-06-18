package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.entity.Comment;
import com.bbs.content.mapper.CommentMapper;
import com.bbs.content.service.CommentService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *
 */
@Service
public class CommentServiceImpl extends MPJBaseServiceImpl<CommentMapper, Comment>
    implements CommentService{



    /**
     * 查询评论、点赞
     * @param newId 内容id
     * @param current 第几页
     * @param size 几条
     * @return GetUserNewsDto.CommentByNewIdDto
     */
    @Override
    public Page<GetUserNewsDto.CommentByNewIdDto> getPageByNewId(Long newId,Long parentId, Integer current, Integer size) {
        Page<GetUserNewsDto.CommentByNewIdDto> result = selectJoinListPage(new Page<>(current, size),GetUserNewsDto.CommentByNewIdDto.class,new MPJLambdaWrapper<Comment>()
                .selectAll(Comment.class)
                .eq(Comment::getNewId, newId)
                .eq(Objects.isNull(parentId),Comment::getParentId,0)
                .eq(Comment::getDeleteFlag,0)
                .eq(Objects.nonNull(parentId),Comment::getParentId,parentId)
        );
        return result;
    }

    @Override
    public Boolean delById(Long commentId) {
        Comment one = lambdaQuery().eq(Comment::getId, commentId).one();
        if (Objects.nonNull(one)) {
            Comment parent = lambdaQuery().eq(Comment::getId, one.getParentId()).one();
            if (Objects.nonNull(parent)) {
                lambdaUpdate().set(Comment::getCommentNum,parent.getCommentNum()-1).eq(Comment::getId,parent.getId()).update();
                return updateById(new Comment().setId(commentId).setDeleteFlag(1));
            }{
                lambdaUpdate().set(Comment::getDeleteFlag,1).eq(Comment::getParentId,commentId).update();
                return updateById(new Comment().setId(commentId).setDeleteFlag(1));
            }
        }
        return false;
    }


}




