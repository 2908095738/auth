package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 员工工资扩展项目值表
 * @TableName employee_item_extend
 */
@TableName(value ="employee_item_extend")
@Data
@Accessors(chain = true)
public class EmployeeItemExtend implements Serializable {
    /**
     * 类型id
     */
    @TableField(value = "item_type_id")
    private Long itemTypeId;

    /**
     * 关联的工资表id
     */
    @TableField(value = "salary_id")
    private Long salaryId;

    /**
     * 关联的员工ID
     */
    @TableField(value = "employee_id")
    private Long employeeId;

    /**
     * 类型字段对应的内容
     */
    @TableField(value = "content")
    private Long content;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}