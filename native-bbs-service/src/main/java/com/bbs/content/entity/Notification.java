package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 通知
 * @TableName notification
 */
@TableName(value ="notification")
@Data
public class Notification implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "notifier")
    private Long notifier;

    /**
     * 
     */
    @TableField(value = "receiver")
    private Long receiver;

    /**
     * 
     */
    @TableField(value = "outerid")
    private Long outerid;

    /**
     * 1回问，2回评，3收藏，4点赞
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 
     */
    @TableField(value = "create_id")
    private Long createId;

    /**
     * 0未读，1已读
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 通知人
     */
    @TableField(value = "notifier_name")
    private String notifierName;

    /**
     * 其他标题
     */
    @TableField(value = "outer_title")
    private String outerTitle;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}