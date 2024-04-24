package com.bbs.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.chat.dto.*;
import com.bbs.chat.dto.param.CreateChatParam;
import com.bbs.chat.service.ChatService;
import com.bbs.chat.util.ThreadLocalUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息
 */
@Api(tags = "消息控制器")
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService service) {
        this.chatService = service;
    }

    @ApiOperation(value = "创建消息", tags = {"消息相关"}, httpMethod = "PUT", consumes = "application/json", produces = "application/json")
    @PutMapping("/createChat")
    public Result<ChatRecordDto> createChat(@RequestBody CreateChatParam param) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        return chatService.createChat(param, userId);
    }

    /**
     * 获取消息页顶部的点赞/收藏、系统通知、评论未读数量
     */
    @ApiOperation(value = "消息页顶部未读通知", notes = "获取消息页顶部的点赞/收藏、系统通知、评论未读数量", tags = {"通知相关"}, httpMethod = "GET", produces = "application/json")
    @GetMapping("/getTop")
    public Result<ChatTopDto> getTop() {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        ChatTopDto result = chatService.getChatTop(userId);
        return Result.success(result);
    }

    /**
     * 获取消息页下方消息列表
     *
     * @param current 页码
     * @param size    条数
     */
    @ApiOperation(value = "获取消息列表", notes = "获取消息页下方消息列表", tags = {"消息相关"}, httpMethod = "GET", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", defaultValue = "1", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "size", value = "条数", defaultValue = "10", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "10")

    })
    @GetMapping("/getChat")
    public Result<ChatListDto> getChat(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        return chatService.getChat(userId, current, size);
    }

    /**
     * 获取聊天记录
     *
     * @param sendUid 发送方用户id
     * @param current 页码
     * @param size    条数
     */
    @ApiOperation(value = "获取聊天记录", tags = {"消息相关"}, httpMethod = "GET", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sendUid", value = "发送方用户id", allowableValues = "[1,infinity]", required = true, dataTypeClass = Long.class, example = "1"),
            @ApiImplicitParam(name = "current", value = "页码", defaultValue = "1", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "size", value = "条数", defaultValue = "10", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "10")

    })
    @GetMapping("/getRecord")
    public Result<Page<ChatRecordDto>> getRecord(Long sendUid, Integer current, Integer size) {
        Page<ChatRecordDto> result = chatService.getRecord(sendUid, current, size);
        return Result.success(result);
    }

    /**
     * 获取点赞/收藏列表
     *
     * @param current 页码
     * @param size    条数
     * @return
     */
    @ApiOperation(value = "获取点赞/收藏列表", tags = {"通知相关"}, httpMethod = "GET", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", defaultValue = "1", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "size", value = "条数", defaultValue = "10", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "10")

    })
    @GetMapping("/getAgree")
    public Result<Page<AgreeDto>> getAgree(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Page<AgreeDto> result = chatService.getAgree(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取关注
     *
     * @param current 页码
     * @param size    条数
     * @return
     */
    //TODO 暂时隐藏，可能用不到
    @ApiOperation(value = "获取关注", hidden = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", defaultValue = "1", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "size", value = "条数", defaultValue = "10", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "10")

    })
    @GetMapping("/getFan")
    public Result<Page<FanDto>> getFan(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Page<FanDto> result = chatService.getFan(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取评论
     *
     * @param current 页码
     * @param size    条数
     * @return
     */
    @ApiOperation(value = "获取评论", tags = {"通知相关"}, httpMethod = "GET", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", defaultValue = "1", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "size", value = "条数", defaultValue = "10", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "10")

    })
    @GetMapping("/getComm")
    public Result<Page<CommDto>> getComm(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Page<CommDto> result = chatService.getComm(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取关注用户昵称
     *
     * @param current 页码
     * @param size    条数
     */
    @ApiOperation(value = "获取关注用户名称", tags = {"消息相关"}, httpMethod = "GET", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", defaultValue = "1", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "size", value = "条数", defaultValue = "10", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "10")

    })
    @GetMapping("/getNick")
    public Result<Page<String>> getNick(Integer current, Integer size) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Page<String> result = chatService.getNick(userId, current, size);
        return Result.success(result);
    }

    /**
     * 互相关注
     * //TODO 应该用不上了
     *
     * @param sendUid 发送方id
     * @param type    互关标识符：1.互关2.取消互关
     */
    @ApiOperation(value = "互相关注", hidden = true)
    @GetMapping("/toFan")
    public Result toFan(Long sendUid, Integer type) {
        Long acceptUid = ThreadLocalUtil.getCurrentUserId();
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
    @ApiOperation(value = "删除消息", tags = "消息相关", httpMethod = "DELETE", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "消息id列表", defaultValue = "[1]", allowMultiple = true, required = true, dataTypeClass = Long.class, example = "[1]"),
            @ApiImplicitParam(name = "type", value = "删除类型: 1.消息页删除;2.聊天框删除", allowableValues = "[1,2]", required = true, dataTypeClass = Integer.class, example = "1")

    })
    @DeleteMapping("/delChat")
    public Result delChat(@RequestParam List<Long> ids, Integer type) {
        chatService.delChat(ids, type);
        return Result.success();
    }

}