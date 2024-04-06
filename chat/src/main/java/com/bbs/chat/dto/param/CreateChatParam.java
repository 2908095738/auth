package com.bbs.chat.dto.param;

import lombok.Data;

import java.util.Date;

@Data
public class CreateChatParam {
    /**
     * 消息发送用户id
     */
    private Long sendUid;

    /**
     * 消息接收用户id
     */
    private Long acceptUid;

    /**
     * 消息类型
     * 1.文本消息
     * 2.图片消息
     * 3.视频消息
     */
    private Integer contentType;

    /**
     * 消息内容
     * 如果是图片、视频就存入url
     */
    private String content;

    /**
     * 消息发送时间
     */
    private Date time;
}