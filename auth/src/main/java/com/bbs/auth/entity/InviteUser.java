package com.bbs.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 邀请用户记录
 * @TableName invite_user
 */
@TableName(value ="invite_user")
@Data
public class InviteUser implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发出邀请用户 ID
     */
    @TableField(value = "initiator_user_id")
    private Long initiatorUserId;

    /**
     * 受邀用户 ID
     */
    @TableField(value = "invited_user_id")
    private Long invitedUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public InviteUser(Long initiatorUserId, Long invitedUserId) {
        this.initiatorUserId = initiatorUserId;
        this.invitedUserId = invitedUserId;
    }
}