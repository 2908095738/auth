package com.bbs.content.controller;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.converter.CommentConverter;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.MqCommentDto;
import com.bbs.content.dto.param.CreateCommentParam;
import com.bbs.content.entity.Comment;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import com.bbs.content.service.CommentService;
import com.bbs.content.util.AuthUtil;
import com.bbs.content.util.ThreadLocalUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 * 评论
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private CommentService service;
    private CommentConverter converter;
    private RabbitmqSend rabbitmqSend;
    private ThumbCache thumbCache;

    private AuthUtil.UserAPI api;

    /**
     * 添加评论
     * @param param param
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createComment(@RequestBody @Valid CreateCommentParam param){
        Comment comment = converter.toEntity(param);

        Date now = new Date();
        comment.setCreateTime(now);
        comment.setUpdateTime(now);
        comment.setCreateId(ThreadLocalUtil.getCurrentUserId());
        if(Objects.isNull(param.getParentId())){
            comment.setParentId(0L);
            comment.setCommentNum(0);
        }{
            Comment parent = service.getById(param.getParentId());
            parent.setCommentNum(parent.getCommentNum()+1);
            service.updateById(parent);
        }
        service.save(comment);
        //通知对应的用户
        MqCommentDto mqCommentDto = converter.toMqDto(param);
        mqCommentDto.setTime(now);
        rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM, RabbitmqConfig.ROUTINGKEY_COMMENT, JSON.toJSONString(mqCommentDto));
        return Result.success(true);
    }

    @Data
    private static class DeleteComment{
        private Long commentId;
    }

    /**
     * 删除评论
     */
    @Transactional
    @DeleteMapping
    public Result<Boolean> deleteComment(@RequestBody DeleteComment param){
        return Result.success(service.delById(param.getCommentId()));
    }




    /**
     *根据主键查评论、点赞
     * @param newId 文章id
     * @param current 第几页
     * @param size 几条
     * @return Page<GetUserNewsDto.CommentByNewIdDto>
     */
    @GetMapping
    public Result<Page<GetUserNewsDto.CommentByNewIdDto>> getPageByNewId(@NotNull(message = "内容id不能为空！") Long newId,
                                                                         Long parentId,
                                                                         @NotNull(message = "页数不能为空！") Integer current,
                                                                         @NotNull(message = "每页几条不能为空！") Integer size){
        Long currentUserId = ThreadLocalUtil.getCurrentUserId();

        Page<GetUserNewsDto.CommentByNewIdDto> result = service.getPageByNewId(newId, parentId, current, size);
        if(isNotEmpty(result.getRecords())) {
            List<Long> commentIds = new ArrayList<>();
            Set<Long> userIds = new HashSet<>();
            result.getRecords().forEach(comment->{
                userIds.add(comment.getCreateId());
                commentIds.add(comment.getId());
            });

            Map<Long, AuthUtil.UserAPI.VO> userIdMap = new HashMap<>();
            if(CollUtil.isNotEmpty(userIds)&&userIds.size()>1){
                userIdMap = api.getUserList(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(AuthUtil.UserAPI.VO::getId, o2 -> o2));
            }{
                AuthUtil.UserAPI.VO userByid = api.getUserByid(new ArrayList<>(userIds).get(0));
                userIdMap.put(userByid.getId(),userByid);
            }
            Map<Long, Set<Long>> commentThumbUsersMap = (Map<Long, Set<Long>>) thumbCache.countBy(newId, null, commentIds, 3);

            for(GetUserNewsDto.CommentByNewIdDto comment:result.getRecords()){
                AuthUtil.UserAPI.VO vo = userIdMap.get(comment.getCreateId());
                Set<Long> thumbUserIds = commentThumbUsersMap.get(comment.getId());
                comment.setAvatar(vo.getAvatar());//头像
                comment.setNickName(vo.getName());//名字
                comment.setHasLike(thumbUserIds.contains(currentUserId));//是否点赞
                comment.setAllowDelete(Objects.equals(comment.getCreateId(), currentUserId));//是否可以删除此评论（自己评论或管理员）
                comment.setLikeNum(thumbUserIds.size());//点赞数
            }
        }
        return Result.success(result);
    }







    @Autowired
    public CommentController(CommentService service, CommentConverter converter, RabbitmqSend rabbitmqSend, ThumbCache thumbCache, AuthUtil.UserAPI api) {
        this.service = service;
        this.converter = converter;
        this.rabbitmqSend = rabbitmqSend;
        this.thumbCache = thumbCache;
        this.api = api;
    }

}
