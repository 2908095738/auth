package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 核算项目类型表
 * @TableName salary_accounting_item_type
 */
@TableName(value ="salary_accounting_item_type")
@Data
@Accessors(chain = true)
public class SalaryAccountingItemType implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 核算项目类型的名称
     */
    @TableField(value = "type_name")
    private String typeName;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}