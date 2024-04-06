package com.bbs.chat.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 聊天记录
 */
@Data
public class ChatRecordDto {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送消息的用户id；-1表示我发给对方
     */
    @TableField(exist = false)
    private Long chatUid;

    /**
     * 消息类型
     * 不同消息类型方便前端处理
     * 1.文本消息
     * 2.图片消息
     * 3.视频消息
     */
    @TableField(value = "content_type")
    private Integer contentType;

    /**
     * 消息内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 消息发送方
     * 1.我发给对方
     * 2.对方发给我
     * TODO 方便前端区分是谁发送的消息，但因为其不是表中的字段,涉及暂不会的子查询故使用[chatUid]代替
     */
//    @TableField(exist = false)
//    private Integer type;

    @TableField(value = "time")
    private Date time;
}