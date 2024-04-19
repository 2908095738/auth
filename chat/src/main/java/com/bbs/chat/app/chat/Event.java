package com.bbs.chat.app.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {

    /**
     * 发送用户 ID
     */
    private Long sourceUID;

    /**
     * 目标用户 ID
     */
    private Long targetUID;

    /**
     * 消息类型（默认文本）
     */
    private Integer type;

    /**
     * 消息发送时间
     */
    private String time;
}