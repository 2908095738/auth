package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 内容
 * @TableName content
 */
@TableName(value ="content")
@Data
public class Content implements Serializable {
    /**
     * 主键
     */
    @TableField(value = "id")
    private Long id;

    /**
     * 内容
     */
    @TableField(value = "html")
    private String html;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public Content(String html) {
        this.html = html;
    }
}