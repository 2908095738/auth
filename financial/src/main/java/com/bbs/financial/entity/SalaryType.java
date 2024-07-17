package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 工资类型表
 * @TableName salary_type
 */
@TableName(value ="salary_type")
@Data
@Accessors(chain = true)
public class SalaryType implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工资类型的名称
     */
    @TableField(value = "type_name")
    private String typeName;

    /**
     * 公司id
     */
    @TableField(value = "accounting_set_id")
    private Long accountingSetId;

    /**
     * 工资类型创建的时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 工资类型最后更新的时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 0表示未删除，1表示已删除
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}