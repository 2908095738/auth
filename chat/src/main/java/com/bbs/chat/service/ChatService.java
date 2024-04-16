package com.bbs.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.chat.dto.*;
import com.bbs.chat.dto.param.CreateChatParam;
import com.bbs.chat.entity.Chat;

import java.util.List;

public interface ChatService extends IService<Chat> {
    Result createChat(CreateChatParam param, Long userId);

    /**
     * 获取消息页顶部的点赞/收藏、关注、评论角标
     *
     * @param userId
     * @return
     */
    ChatTopDto getChatTop(Long userId);

    /**
     * 获取消息页下方消息列表
     *
     * @param userId
     * @param current 未读消息第几页
     * @param size    未读消息几条
     * @return
     */
    Result<ChatListDto> getChat(Long userId, Integer current, Integer size);

    /**
     * 获取聊天记录
     *
     * @param targetUID   聊天对方 UID
     * @param current   第几页
     * @param size      几条
     */
    Page<ChatRecordDto> getRecord(Long targetUID, Integer current, Integer size);

    /**
     * 获取点赞、收藏列表
     *
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @return
     */
    Page<AgreeDto> getAgree(Long userId, Integer current, Integer size);

    /**
     * 获取粉丝
     *
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @return
     */
    Page<FanDto> getFan(Long userId, Integer current, Integer size);

    /**
     * 获取评论
     *
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @return
     */
    Page<CommDto> getComm(Long userId, Integer current, Integer size);

    /**
     * 获取关注用户昵称列表
     *
     * @param userId  用户id
     * @param current 第几页
     * @param size    几条
     * @return
     */
    Page<String> getNick(Long userId, Integer current, Integer size);

    /**
     * 互关
     *
     * @param sendUid   发送方id
     * @param acceptUid 接收方id
     * @param type      互关标识符：1.互关2.取消互关
     * @return
     */
    Result toFan(Long sendUid, Long acceptUid,Integer type);

    /**
     * 删除消息
     *
     * @param chatIds 消息id列表
     * @param type    1.消息页删除
     *                2.聊天框删除
     * @return
     */
    void delChat(List<Long> chatIds, Integer type);
}