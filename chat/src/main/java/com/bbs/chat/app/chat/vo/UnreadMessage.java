package com.bbs.chat.app.chat.vo;

import com.bbs.chat.app.chat.queue.UserMessageQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadMessage {

    private Long id;
    /**
     * 头像 URL 地址
     */
    private String avatar;

    private String name;

    private String email;

    private String phone;

    private String token;

    private Long failureTokenTime;

    /**
     * 是否关注了当前登录用户
     */
    private Boolean isFollow;

    private List<UserMessageQueue.Message> unreadMessage;
}
