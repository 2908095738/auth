package com.bbs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 
 * @TableName new_content
 */
@TableName(value ="new_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewContent implements Serializable {
    /**
     * 文章id
     */
    @TableId(value = "new_id")
    private Long newId;

    /**
     * 全部内容
     */
    @TableField(value = "content")
    private String content;

    public NewContent(Long newId) {
        this.newId = newId;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}