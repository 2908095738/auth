package com.bbs.financial.enums;

//工号, 姓名, 部门, 身份证号码, 计薪日, 出勤天数, 基本工资, 出勤工资,
//奖金, 津贴, 补贴, 其他应发1, 其他应发2, 应发合计, 养老保险, 医疗保险, 失业保险,
//公积金, 累计应发, 累计社保公积金, 累计子女教育, 累计住房贷款利息, 累计住房租金,
//累计赡养父母, 累计继续教育, 累计婴幼儿照护费用, 累计应缴个税, 累计已缴个税, 本月应缴个税,
//个人还款, 其他扣款1, 其他扣款2, 实发工资

public enum SalayExeclHeaderEnum {

    ID(0, "工号"),
    NAME(1, "姓名"),
    DEPARTMENT(2, "部门"),
    ID_NUMBER(3, "身份证号码"),
    PAYROLL_DATE(4, "计薪日"),
    ATTENDANCE_DAYS(5, "出勤天数"),
    BASE_SALARY(6, "基本工资"),
    ATTENDANCE_WAGE(7, "出勤工资"),
    BONUS(8, "奖金"),
    ALLOWANCE(9, "津贴"),
    SUBSIDY(10, "补贴"),
    OTHER_INCOME_1(11, "其他应发1"),
    OTHER_INCOME_2(12, "其他应发2"),
    TOTAL_INCOME(13, "应发合计"),
    PENSION_INSURANCE(14, "养老保险"),
    MEDICAL_INSURANCE(15, "医疗保险"),
    UNEMPLOYMENT_INSURANCE(16, "失业保险"),
    HOUSING_FUND(17, "公积金"),
    ACCUMULATED_TOTAL_INCOME(18, "累计应发"),
    ACCUMULATED_SOCIAL_INSURANCES(19, "累计社保公积金"),
    ACCUMULATED_CHILD_EDUCATION(20, "累计子女教育"),
    ACCUMULATED_MORTGAGE_INTEREST(21, "累计住房贷款利息"),
    ACCUMULATED_RENT(22, "累计住房租金"),
    ACCUMULATED_SUPPORTING_PARENTS(23, "累计赡养父母"),
    ACCUMULATED_CONTINUING_EDUCATION(24, "累计继续教育"),
    ACCUMULATED_INFANT_CARE_FEES(25, "累计婴幼儿照护费用"),
    ACCUMULATED_TAX_DUE(26, "累计应缴个税"),
    ACCUMULATED_TAX_PAID(27, "累计已缴个税"),
    MONTHLY_TAX_DUE(28, "本月应缴个税"),
    PERSONAL_REPAYMENT(29, "个人还款"),
    OTHER_DEDUCTION_1(30, "其他扣款1"),
    OTHER_DEDUCTION_2(31, "其他扣款2"),
    NET_SALARY(32, "实发工资");

    private int key;
    private String value;

    SalayExeclHeaderEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static int getKeyByValue(String value) {
        for (SalayExeclHeaderEnum field : values()) {
            if (field.getValue().equals(value)) {
                return field.getKey();
            }
        }
        throw new IllegalArgumentException("找不到对应的键: " + value);
    }

    public static String getValueByKey(int key) {
        for (SalayExeclHeaderEnum field : values()) {
            if (field.getKey() == key) {
                return field.getValue();
            }
        }
        throw new IllegalArgumentException("找不到对应的值: " + key);
    }


}
