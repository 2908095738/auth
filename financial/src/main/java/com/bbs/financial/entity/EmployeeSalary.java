package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工工资表
 * @TableName employee_salary
 */
@TableName(value ="employee_salary")
@Data
public class EmployeeSalary implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 用户名称
     */
    @TableField(value = "employee_name")
    private String employeeName;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private Long phone;

    /**
     * 身份证号
     */
    @TableField(value = "id_card")
    private String idCard;

    /**
     * 工号
     */
    @TableField(value = "job_card")
    private String jobCard;


    /**
     * 应发工资
     */
    @TableField(value = "gross_amount")
    private Long grossAmount;


    /**
     * 员工实际收到的工资金额
     */
    @TableField(value = "net_amount")
    private Long netAmount;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}