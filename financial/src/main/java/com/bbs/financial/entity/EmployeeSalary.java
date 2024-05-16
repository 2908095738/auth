package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 工资表
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
     * 关联的员工ID
     */
    @TableField(value = "employee_id")
    private Integer employeeId;

    /**
     * 工资日期
     */
    @TableField(value = "period_date")
    private Date periodDate;

    /**
     * 员工在该周期内的应发工资总额
     */
    @TableField(value = "gross_amount")
    private BigDecimal grossAmount;

    /**
     * 在应发工资中扣除的总额（如税费、保险等）
     */
    @TableField(value = "deductions_total")
    private BigDecimal deductionsTotal;

    /**
     * 员工实际收到的工资金额
     */
    @TableField(value = "net_amount")
    private BigDecimal netAmount;

    /**
     * 工资单的审批状态
     */
    @TableField(value = "approval_status")
    private Object approvalStatus;

    /**
     * 工资单审批通过的日期
     */
    @TableField(value = "approval_date")
    private Date approvalDate;

    /**
     * 审批工资单的员工ID
     */
    @TableField(value = "approver_id")
    private Integer approverId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}