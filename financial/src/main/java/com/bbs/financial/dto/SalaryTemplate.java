package com.bbs.financial.dto;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class SalaryTemplate {

    @NotBlank
    @Alias(value = "工号")
    private String jobId;

    @NotBlank
    @Alias(value = "姓名")
    private String name;

    @NotBlank
    @Alias(value = "部门")
    private String groupName;

    @NotBlank
    @Alias(value = "身份证号码")
    private String idCard;

    @NotBlank
    @Alias(value = "手机号")
    private String phone;

    @NotBlank
    @Alias(value = "应发工资")
    private BigDecimal grossPay;

    @NotBlank
    @Alias(value = "实发工资")
    private BigDecimal netPay;


    @NotBlank
    @Alias(value = "计薪日")
    private Integer payDays;

    @NotBlank
    @Alias(value = "出勤天数")
    private Integer attendanceDays;

    @NotBlank
    @Alias(value = "基本工资")
    private BigDecimal baseAmount;

    @Alias(value = "出勤工资")
    private BigDecimal attendanceAmount;

    @Alias(value = "奖金")
    private BigDecimal bonus;

    @Alias(value = "津贴")
    private BigDecimal allowance;

    @Alias(value = "补贴")
    private BigDecimal subsidy;

    @Alias(value = "其他应发1")
    private BigDecimal otherEarnings1;

    @Alias(value = "其他应发2")
    private BigDecimal otherEarnings2;

    @Alias(value = "养老保险")
    private BigDecimal pensionInsurance;

    @Alias(value = "医疗保险")
    private BigDecimal medicalInsurance;

    @Alias(value = "失业保险")
    private BigDecimal unemploymentInsurance;

    @Alias(value = "公积金")
    private BigDecimal housingFund;

    @NotBlank
    @Alias(value = "累计应发")
    private BigDecimal totalGross;

    @Alias(value = "累计社保公积金")
    private BigDecimal totalSocialSecurityAndFund;

    @Alias(value = "累计子女教育")
    private BigDecimal totalChildEducation;

    @Alias(value = "累计住房贷款利息")
    private BigDecimal totalHousingLoanInterest;

    @Alias(value = "累计住房租金")
    private BigDecimal totalRent;

    @Alias(value = "累计赡养父母")
    private BigDecimal totalParentSupport;

    @Alias(value = "累计继续教育")
    private BigDecimal totalContinuingEducation;

    @Alias(value = "累计婴幼儿照护费用")
    private BigDecimal totalChildCareExpenses;

    @Alias(value = "累计应缴个税")
    private BigDecimal totalTaxDue;

    @Alias(value = "累计已缴个税")
    private BigDecimal totalTaxPaid;

    @Alias(value = "本月应缴个税")
    private BigDecimal monthlyTaxDue;

    @Alias(value = "个人还款")
    private BigDecimal personalRepayment;

    @Alias(value = "其他扣款1")
    private BigDecimal otherDeductions1;

    @Alias(value = "其他扣款2")
    private BigDecimal otherDeductions2;

}
