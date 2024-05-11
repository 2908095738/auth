package com.bbs.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 内容审核
 * @TableName audit
 */
@TableName(value ="audit")
@Data
public class Audit implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 内容 no
     */
    @TableField(value = "no")
    private String no;

    /**
     * 审核员 ID
     */
    @TableField(value = "auditor_id")
    private Long auditorId;

    /**
     * 审核状态（0待审核；1通过；-1不通过）
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 创建id
     */
    @TableField(value = "create_id")
    private Long createId;

    /**
     * 发表时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改id
     */
    @TableField(value = "update_id")
    private Long updateId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    public Audit(String no, Long createId) {
        this.no = no;
        this.createId = createId;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}