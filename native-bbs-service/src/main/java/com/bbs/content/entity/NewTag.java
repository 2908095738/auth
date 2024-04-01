package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 
 * @TableName new_tag
 */
@TableName(value ="new_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewTag implements Serializable {
    /**
     * 内容id
     */
    @TableField(value = "new_id")
    private Long newId;

    /**
     * 标签id
     */
    @TableField(value = "tag_id")
    private Long tagId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}