package com.bbs.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 邀请
 *
 * @TableName invite
 */
@TableName(value = "invite")
@Data
public class Invite implements Serializable {
    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 邀请码
     */
    @TableField(value = "invite_code")
    private String inviteCode;

    @TableField(value = "valid_end_time")
    private Date validEndTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}