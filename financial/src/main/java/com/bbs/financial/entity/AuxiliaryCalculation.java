package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 核算项目类型表
 * @TableName salary_accounting_item_type
 */
@TableName(value ="auxiliary_calculation")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuxiliaryCalculation implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 核算项目类型的名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 公司ID
     */
    @TableField(value = "company_id")
    private Long companyId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}