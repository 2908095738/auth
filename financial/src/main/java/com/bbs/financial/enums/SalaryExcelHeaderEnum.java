package com.bbs.financial.enums;


public enum SalaryExcelHeaderEnum {

    JOB_ID("jobId", "工号"),
    NAME("name", "姓名"),
    GROUP_NAME("groupName", "部门"),
    ID_CARD("idCard", "身份证号码"),
    PHONE("phone", "手机号"),
    GROSS_PAY("grossPay", "应发工资"),
    NET_PAY("netPay", "实发工资"),


    PAY_DAY("payDay", "计薪日"),
    ATTENDANCE_DAYS("attendanceDays", "出勤天数"),
    BASE_AMOUNT("baseAmount", "基本工资"),
    ATTENDANCE_AMOUNT("attendanceAmount", "出勤工资"),
    BONUS("bonus", "奖金"),
    ALLOWANCE("allowance", "津贴"),
    SUBSIDY("subsidy", "补贴"),
    OTHER_EARNINGS_1("otherEarnings1", "其他应发1"),
    OTHER_EARNINGS_2("otherEarnings2", "其他应发2"),
    PENSION_INSURANCE("pensionInsurance", "养老保险"),
    MEDICAL_INSURANCE("medicalInsurance", "医疗保险"),
    UNEMPLOYMENT_INSURANCE("unemploymentInsurance", "失业保险"),
    HOUSING_FUND("housingFund", "公积金"),
    TOTAL_GROSS("totalGross", "累计应发"),
    TOTAL_SOCIAL_SECURITY_AND_FUND("totalSocialSecurityAndFund", "累计社保公积金"),
    TOTAL_CHILD_EDUCATION("totalChildEducation", "累计子女教育"),
    TOTAL_HOUSING_LOAN_INTEREST("totalHousingLoanInterest", "累计住房贷款利息"),
    TOTAL_RENT("totalRent", "累计住房租金"),
    TOTAL_PARENT_SUPPORT("totalParentSupport", "累计赡养父母"),
    TOTAL_CONTINUING_EDUCATION("totalContinuingEducation", "累计继续教育"),
    TOTAL_CHILD_CARE_EXPENSES("totalChildCareExpenses", "累计婴幼儿照护费用"),
    TOTAL_TAX_DUE("totalTaxDue", "累计应缴个税"),
    TOTAL_TAX_PAID("totalTaxPaid", "累计已缴个税"),
    MONTHLY_TAX_DUE("monthlyTaxDue", "本月应缴个税"),
    PERSONAL_REPAYMENT("personalRepayment", "个人还款"),
    OTHER_DEDUCTIONS_1("otherDeductions1", "其他扣款1"),
    OTHER_DEDUCTIONS_2("otherDeductions2", "其他扣款2");


    private final String fieldName;
    private final String displayName;

    SalaryExcelHeaderEnum(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }



    public static String getFieldNameByDisplayName(String displayName) {
        for (SalaryExcelHeaderEnum value : values()) {
            if (value.getDisplayName().equals(displayName)) {
                return value.getFieldName();
            }
        }
        return null;
    }
}

