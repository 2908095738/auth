package com.bbs.chat.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.chat.entity.ChatTop;
import com.bbs.chat.entity.Comment;
import com.bbs.chat.entity.News;
import com.bbs.chat.enums.DBType;
import com.bbs.chat.mapper.ChatTopMapper;
import com.bbs.chat.mapper.CommentMapper;
import com.bbs.chat.service.CommentService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
        //TODO 规避自己给自己评论，出现角标的问题

        LambdaQueryChainWrapper<Comment> wrapper = lambdaQuery()
                .eq(Comment::getNewId, comment.getNewId())
                .eq(Comment::getCreateId, comment.getCreateId())
                .eq(Comment::getStatus, 20)
                .eq(Comment::getDeleteFlag, 0);

        if (Objects.nonNull(comment.getParentId())) {
            wrapper.eq(Comment::getParentId, comment.getParentId());
        } else {
            wrapper.isNull(Comment::getParentId);
        }

        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        List<Comment> tmps = wrapper.list();

        Comment tmp = null;
        //TODO 应急措施，表字段难以确定唯一性，退而求其次选择用时间确定
        for (Comment now : tmps) {
            long f = now.getCreateTime().getTime();
            long in = comment.getCreateTime().getTime();
            if (in - f < 600) {//TODO 根据创建时间的差值确定唯一性，这个时间可能要动态调整，当然有其他方式确定唯一性更好
                tmp = now;
                break;
            }
        }

        if (Objects.nonNull(tmp)) {
            if (Objects.nonNull(tmp.getParentId()) && tmp.getParentId() != null) {//回复评论
                Comment nowC = lambdaQuery()
                        .select(Comment::getCreateId)
                        .eq(Comment::getId, tmp.getParentId())
                        .eq(Comment::getDeleteFlag, 0)
                        .eq(Comment::getStatus, 20)
                        .one();

                if (Objects.isNull(nowC)) {
                    return Result.success("parent comment del");
                }
                Long acceptUid = nowC.getCreateId();

                DynamicDataSourceContextHolder.poll();
                DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());

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

                DynamicDataSourceContextHolder.poll();

                return Result.success();
            } else {//评论文章
                MPJLambdaWrapper acceptWrap = new MPJLambdaWrapper<Comment>()//接收通知的用户id
                        .select(News::getCreateId)
                        .leftJoin(News.class, News::getNewId, Comment::getNewId)
                        .isNull(Comment::getParentId)
                        .eq(Comment::getId, tmp.getId())
                        .eq(Comment::getStatus, 20);

                Long acceptUid = (Long) commentMapper.selectJoinMap(acceptWrap).get("create_id");

                DynamicDataSourceContextHolder.poll();
                DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());

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

                DynamicDataSourceContextHolder.poll();

                return Result.success();
            }
        } else {
            return Result.failed("db no data");
        }
    }
}