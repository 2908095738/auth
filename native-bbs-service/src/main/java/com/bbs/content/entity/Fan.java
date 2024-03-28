package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 粉丝和关注表
 * @TableName fan
 */
@TableName(value ="fan")
@Data
public class Fan implements Serializable {
    /**
     * 账号id
     */
    @TableId(value = "account_id")
    private Long accountId;

    /**
     * 粉丝账号id或关注账号id
     */
    @TableField(value = "f_account_id")
    private Long fAccountId;

    /**
     * 类型：0粉丝 1关注
     */
    @TableField(value = "type")
    private Integer type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}