package com.bbs.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.chat.converter.CommentConverter;
import com.bbs.chat.converter.ThumbConverter;
import com.bbs.chat.dto.*;
import com.bbs.chat.dto.param.CreateChatParam;
import com.bbs.chat.service.ChatService;
import com.bbs.chat.service.ThumbService;
import com.bbs.chat.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private ChatService chatService;

    private CommentConverter converter;

    private ThumbConverter thumbConverter;

    @Autowired
    public ChatController(ChatService service, ThumbService thumbService, CommentConverter converter, ThumbConverter thumbConverter) {
        this.chatService = service;
        this.converter = converter;
        this.thumbConverter = thumbConverter;
    }

    @PutMapping("/createChat")
    public Result createChat(@RequestBody CreateChatParam param) {
        Long userId = ThreadLocalUtil.getCurrentUser().getUserId();
        return chatService.createChat(param, userId);
    }

    /**
     * 获取消息页顶部的点赞/收藏、关注、评论角标
     *
     * @return
     */
    @GetMapping("/getTop")
    public Result<ChatTopDto> getTop() {
        Long userId = ThreadLocalUtil.getCurrentUser().getUserId();
        ChatTopDto result = chatService.getChatTop(userId);
        return Result.success(result);
    }

    /**
     * 获取消息页下方消息列表
     *
     * @param current 未读消息第几页
     * @param size    未读消息几条
     * @return
     */
    @GetMapping("/getChat")
    public Result<ChatListDto> getChat(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUser().getUserId();
        ChatListDto result = chatService.getChat(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取聊天记录
     *
     * @param sendUid 发送方用户id
     * @param current 第几页
     * @param size    几条
     */
    @GetMapping("/getRecord")
    public Result<Page<ChatRecordDto>> getRecord(Long sendUid, Integer current, Integer size) {
        Long acceptUid = ThreadLocalUtil.getCurrentUser().getUserId();
        Page<ChatRecordDto> result = chatService.getRecord(sendUid, acceptUid, current, size);
        return Result.success(result);
    }

    /**
     * 获取点赞、收藏列表
     *
     * @param current 第几页
     * @param size    几条
     * @return
     */
    @GetMapping("/getAgree")
    public Result<Page<AgreeDto>> getAgree(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUser().getUserId();
        Page<AgreeDto> result = chatService.getAgree(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取关注
     *
     * @param current 第几页
     * @param size    几条
     * @return
     */
    @GetMapping("/getFan")
    public Result<Page<FanDto>> getFan(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUser().getUserId();
        Page<FanDto> result = chatService.getFan(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取评论
     *
     * @param current 第几页
     * @param size    几条
     * @return
     */
    @GetMapping("/getComm")
    public Result<Page<CommDto>> getComm(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUser().getUserId();
        Page<CommDto> result = chatService.getComm(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取关注用户昵称
     *
     * @param current
     * @param size
     * @return
     */
    @GetMapping("/getNick")
    public Result<Page<String>> getNick(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUser().getUserId();
        Page<String> result = chatService.getNick(userId, current, size);
        return Result.success(result);
    }

    /**
     * 互相关注
     *
     * @param sendUid   发送方id
     * @param type      互关标识符：1.互关2.取消互关
     * @return
     */
    @GetMapping("/toFan")
    public Result toFan(Long sendUid, Integer type) {
        Long acceptUid = ThreadLocalUtil.getCurrentUser().getUserId();
        if (sendUid.equals(acceptUid))
            return Result.failed("sendUid don't = acceptUid!");
        return chatService.toFan(sendUid, acceptUid, type);
    }

    /**
     * 删除消息
     *
     * @param ids  消息id列表
     * @param type 1.消息页删除
     *             2.聊天框删除
     * @return
     */
    @DeleteMapping("/delChat")
    public Result delChat(@RequestParam List<Long> ids, Integer type) {
        chatService.delChat(ids, type);
        return Result.success();
    }

}