package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
     * 工资日期
     */
    @TableField(value = "period_date")
    private Date periodDate;

    /**
     * 计薪日
     */
    @TableField(value = "pay_day")
    private Integer payDay;

    /**
     * 出勤天数
     */
    @TableField(value = "attendance_days")
    private Integer attendanceDays;

    /**
     * 基本工资
     */
    @TableField(value = "base_amount")
    private Long baseAmount;

    /**
     * 应发工资
     */
    @TableField(value = "gross_pay")
    private Long grossPay;

    /**
     * 出勤工资
     */
    @TableField(value = "attendance_amount")
    private Long attendanceAmount;

    /**
     * 员工实际收到的工资金额
     */
    @TableField(value = "net_amount")
    private Long netAmount;

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
     * 部门名称
     */
    @TableField(value = "company_structure_name")
    private String companyStructureName;

    @TableField(exist = false)
    private List<EmployeeItemExtend> employeeItemExtends;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}