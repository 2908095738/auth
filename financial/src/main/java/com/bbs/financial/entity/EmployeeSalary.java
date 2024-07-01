package com.bbs.financial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
     * 应发工资
     */
    @TableField(value = "gross_pay")
    private BigDecimal grossPay;

    /**
     * 员工实际收到的工资金额
     */
    @TableField(value = "net_pay")
    private BigDecimal netPay;

    /**
     * 用户名称
     */
    @TableField(value = "employee_name")
    private String employeeName;

    /**
     * 工号
     */
    @TableField(value = "job_card")
    private String jobCard;

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
     * 部门名称
     */
    @TableField(value = "company_structure_name")
    private String companyStructureName;

    /**
     * 出勤天数
     */
    @TableField(value = "attendance_days")
    private Integer attendanceDays;

    /**
     * 计薪日
     */
    @TableField(value = "pay_days")
    private Integer payDays;

    /**
     * 基本工资
     */
    @TableField(value = "base_amount")
    private BigDecimal baseAmount;

    /**
     * 出勤工资
     */
    @TableField(value = "attendance_amount")
    private BigDecimal attendanceAmount;

    /**
     * 奖金
     */
    @TableField(value = "bonus")
    private BigDecimal bonus;

    /**
     * 津贴
     */
    @TableField(value = "allowance")
    private BigDecimal allowance;

    /**
     * 补贴
     */
    @TableField(value = "subsidy")
    private BigDecimal subsidy;

    /**
     * 其他应发1
     */
    @TableField(value = "other_earnings_1")
    private BigDecimal otherEarnings1;

    /**
     * 其他应发2
     */
    @TableField(value = "other_earnings_2")
    private BigDecimal otherEarnings2;

    /**
     * 养老保险
     */
    @TableField(value = "pension_insurance")
    private BigDecimal pensionInsurance;

    /**
     * 医疗保险
     */
    @TableField(value = "medical_insurance")
    private BigDecimal medicalInsurance;

    /**
     * 失业保险
     */
    @TableField(value = "unemployment_insurance")
    private BigDecimal unemploymentInsurance;

    /**
     * 公积金
     */
    @TableField(value = "housing_fund")
    private BigDecimal housingFund;

    /**
     * 累计应发
     */
    @TableField(value = "total_gross")
    private BigDecimal totalGross;

    /**
     * 累计社保公积金
     */
    @TableField(value = "total_social_security_and_fund")
    private BigDecimal totalSocialSecurityAndFund;

    /**
     * 累计子女教育
     */
    @TableField(value = "total_child_education")
    private BigDecimal totalChildEducation;

    /**
     * 累计住房贷款利息
     */
    @TableField(value = "total_housing_loan_interest")
    private BigDecimal totalHousingLoanInterest;

    /**
     * 累计住房租金
     */
    @TableField(value = "total_rent")
    private BigDecimal totalRent;

    /**
     * 累计赡养父母
     */
    @TableField(value = "total_parent_support")
    private BigDecimal totalParentSupport;

    /**
     * 累计继续教育
     */
    @TableField(value = "total_continuing_education")
    private BigDecimal totalContinuingEducation;

    /**
     * 累计婴幼儿照护费用
     */
    @TableField(value = "total_child_care_expenses")
    private BigDecimal totalChildCareExpenses;

    /**
     * 累计应缴个税
     */
    @TableField(value = "total_tax_due")
    private BigDecimal totalTaxDue;

    /**
     * 累计已缴个税
     */
    @TableField(value = "total_tax_paid")
    private BigDecimal totalTaxPaid;

    /**
     * 本月应缴个税
     */
    @TableField(value = "monthly_tax_due")
    private BigDecimal monthlyTaxDue;

    /**
     * 个人还款
     */
    @TableField(value = "personal_repayment")
    private BigDecimal personalRepayment;

    /**
     * 其他扣款1
     */
    @TableField(value = "other_deductions_1")
    private BigDecimal otherDeductions1;

    /**
     * 其他扣款2
     */
    @TableField(value = "other_deductions_2")
    private BigDecimal otherDeductions2;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}