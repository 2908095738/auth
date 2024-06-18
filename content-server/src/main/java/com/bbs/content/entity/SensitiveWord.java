package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 敏感词汇
 * @TableName sensitive_word
 */
@TableName(value ="sensitive_word")
@Data
public class SensitiveWord implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "badword")
    private String badword;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}