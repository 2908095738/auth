package com.bbs.chat.app.chat.api;

import com.bbs.Result;
import com.bbs.api.Auth;
import com.bbs.chat.app.chat.cache.ChatListCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
public class ChatList {

    @Resource
    private ChatListCache chatListCache;

    @Resource
    private Auth.UserAPI api;

    @GetMapping("/chats")
    public Result<ChatListCache.VO> getList() {
        Auth.UserAPI.User loginUser = api.getLoginUser();
        return Result.success(chatListCache.getChatList(loginUser.getId()));
    }

    @DeleteMapping("/chats/unread/{id}")
    public Result<Boolean> cleanUnreadMessageFlag(@PathVariable("id") Long chatTargetUID) {
        Auth.UserAPI.User loginUser = api.getLoginUser();
        chatListCache.cleanUnreadMessageFlag(loginUser.getId(), chatTargetUID);
        return Result.success();
    }
}
