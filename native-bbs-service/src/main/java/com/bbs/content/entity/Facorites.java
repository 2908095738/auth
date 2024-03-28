package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 收藏表
 * @TableName facorites
 */
@TableName(value ="facorites")
@Data
public class Facorites implements Serializable {
    /**
     * 
     */
    @TableField(value = "account_id")
    private Long accountId;

    /**
     * 
     */
    @TableField(value = "new_id")
    private Long newId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}