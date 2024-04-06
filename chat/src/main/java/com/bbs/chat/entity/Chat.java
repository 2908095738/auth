package com.bbs.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息
 */
@Data
@TableName(value = "chat")
public class Chat implements Serializable {
    /**
     * 唯一标识符
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息发送方id
     */
    @TableField(value = "send_uid")
    private Long sendUid;

    /**
     * 消息接收方id
     */
    @TableField(value = "accept_uid")
    private Long acceptUid;

    /**
     * 0：该消息可查
     * 1：逻辑删除
     * 在聊天框中，可能用户会删除部分消息
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 消息类型
     * 1.文本消息
     * 2.图片消息
     * 3.视频消息
     */
    @TableField(value = "content_type")
    private Integer contentType;

    /**
     * 消息内容
     * 如果是图片、视频就存入url
     */
    @TableField(value = "content")
    private String content;

    /**
     * 消息发送时间
     */
    @TableField(value = "time", fill = FieldFill.INSERT)
    private Date time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}