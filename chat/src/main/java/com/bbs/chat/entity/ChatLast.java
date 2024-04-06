package com.bbs.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 最新消息
 */
@Data
@TableName(value = "chat_last")
public class ChatLast implements Serializable {
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
     * 未读消息数量
     */
    @TableField(value = "count")
    private Integer count;

    /**
     * 最新消息类型
     * 1.文本消息
     * 2.图片消息
     * 3.视频消息
     */
    @TableField(value = "content_type")
    private Integer contentType;

    /**
     * 最新消息内容
     *  如果是图片、视频就存入url
     */
    @TableField(value = "content_last")
    private String contentLast;

    /**
     * 最新消息时间
     */
    @TableField(value = "time_last", fill = FieldFill.INSERT_UPDATE)
    private Date timeLast;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}