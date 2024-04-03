package com.bbs.content.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.converter.CommentConverter;
import com.bbs.content.dto.GetUserNewsDto;
import com.bbs.content.dto.MqCommentDto;
import com.bbs.content.dto.param.CreateCommentParam;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import com.bbs.content.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 评论
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private CommentService service;
    private CommentConverter converter;
    private RabbitmqSend rabbitmqSend;

    /**
     * 添加评论
     * @param param param
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createComment(@RequestBody @Valid CreateCommentParam param){
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        service.save(converter.toEntity(param));
        //通知对应的用户
        MqCommentDto mqCommentDto = converter.toMqDto(param);
        rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM, RabbitmqConfig.ROUTINGKEY_COMMENT, JSON.toJSONString(mqCommentDto));
        return Result.success(true);
    }



    /**
     * 删除评论
     */
    @DeleteMapping
    public Result<Boolean> deleteComment(@RequestBody Long commentId){
        return service.delById(commentId);
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
                                                                         @NotNull(message = "页数不能为空！") Integer current,
                                                                         @NotNull(message = "每页几条不能为空！") Integer size){
        return Result.success(service.getPageByNewId(newId, current, size));
    }







    @Autowired
    public CommentController(CommentService service, CommentConverter converter, RabbitmqSend rabbitmqSend) {
        this.service = service;
        this.converter = converter;
        this.rabbitmqSend = rabbitmqSend;
    }

}
