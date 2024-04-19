package com.bbs.chat.app.chat.api;

import com.bbs.Result;
import com.bbs.api.Auth;
import com.bbs.chat.app.chat.cache.ChatListCache;
import com.bbs.chat.app.chat.queue.UserMessageQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@Slf4j
@RestController
public class ChatList {

    @Resource
    private ChatListCache chatListCache;

    @Resource
    private Auth.UserAPI api;

    @GetMapping("/chats")
    public Result<List<ChatListCache.Chat>> getList() {
        Auth.UserAPI.User loginUser = api.getLoginUser();
        return Result.success(chatListCache.getChatList(loginUser.getId()));
    }

    @GetMapping("/chats/unread/size")
    public Result<Integer> getUnreadMessageSize() {
        Auth.UserAPI.User loginUser = api.getLoginUser();
        return Result.success(chatListCache.getUnreadMessageSize(loginUser.getId()));
    }

    @DeleteMapping("/chats/unread/{id}")
    public Result<Boolean> cleanUnreadMessageFlag(@PathVariable("id") Long chatTargetUID) {
        Auth.UserAPI.User loginUser = api.getLoginUser();
        chatListCache.cleanUnreadMessageFlag(loginUser.getId(), chatTargetUID);
        return Result.success();
    }
}
