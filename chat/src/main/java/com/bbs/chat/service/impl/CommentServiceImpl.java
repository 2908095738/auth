package com.bbs.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.chat.entity.ChatTop;
import com.bbs.chat.entity.Comment;
import com.bbs.chat.entity.News;
import com.bbs.chat.mapper.ChatTopMapper;
import com.bbs.chat.mapper.CommentMapper;
import com.bbs.chat.service.CommentService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ChatTopMapper chatTopMapper;

    @Override
    public Result createComment(Comment comment) {
        QueryWrapper<Comment> dbWrap = new QueryWrapper();
        dbWrap
                .eq("new_id", comment.getNewId())
                .eq("create_id", comment.getCreateId())
                .eq("status", 20);

        if (Objects.nonNull(comment.getParentId())) {
            dbWrap.eq("parent_id", comment.getParentId());
        } else {
            dbWrap.isNull("parent_id");
        }

        Comment tmp = getOne(dbWrap);

        if (Objects.nonNull(tmp)) {
            if (Objects.nonNull(tmp.getParentId())) {//回复评论
                Long acceptUid = new MPJLambdaWrapper<Comment>(Comment.class)//接收通知的用户id
                        .selectAll(Comment.class)
                        .leftJoin(Comment.class, Comment::getId, Comment::getParentId)
                        .eq(Comment::getId, tmp.getId())
                        .eq(Comment::getStatus, 20)
                        .one()
                        .getCreateId();

                ChatTop tmpTop = new MPJLambdaWrapper<ChatTop>(ChatTop.class)//接收通知的用户未读消息
                        .selectAll(ChatTop.class)
                        .eq(ChatTop::getUserId, acceptUid)
                        .one();

                if (Objects.nonNull(tmpTop)) {  //修改未读消息入库
                    tmpTop.setCommentCount(tmpTop.getCommentCount() + 1);
                    chatTopMapper.updateById(tmpTop);
                } else {//新建未读消息入库
                    tmpTop = new ChatTop();
                    tmpTop.setUserId(acceptUid);
                    tmpTop.setCommentCount(1);
                    chatTopMapper.insert(tmpTop);
                }

                return Result.success();
            } else {//评论文章
                MPJLambdaWrapper acceptWrap = new MPJLambdaWrapper<Comment>()//接收通知的用户id
                        .select(News::getCreateId)
                        .leftJoin(News.class, News::getNewId, Comment::getNewId)
                        .isNull(Comment::getParentId)
                        .eq(Comment::getId, tmp.getId())
                        .eq(Comment::getStatus, 20);

                Long acceptUid = (Long) commentMapper.selectJoinMap(acceptWrap).get("create_id");

                ChatTop tmpTop = new MPJLambdaWrapper<ChatTop>(ChatTop.class)//接收通知的用户未读消息
                        .selectAll(ChatTop.class)
                        .eq(ChatTop::getUserId, acceptUid)
                        .one();

                if (Objects.nonNull(tmpTop)) {  //修改未读消息入库
                    tmpTop.setCommentCount(tmpTop.getCommentCount() + 1);
                    chatTopMapper.updateById(tmpTop);
                } else {//新建未读消息入库
                    tmpTop = new ChatTop();
                    tmpTop.setUserId(acceptUid);
                    tmpTop.setCommentCount(1);
                    chatTopMapper.insert(tmpTop);
                }

                return Result.success();
            }
        } else {
            return Result.failed("db no data");
        }
    }
}