package com.bbs.financial.enums;

public enum CashierExeclEnum {
    //日记账相关映射
    DATE(CashierExeclTypeEnum.NOTE, "日期"),
    NO(CashierExeclTypeEnum.NOTE, "流水号"),
    CERT_ABS(CashierExeclTypeEnum.NOTE, "摘要"),
    HE_ACCOUNT(CashierExeclTypeEnum.NOTE, "账户对方科目"),
    AUXILIART(CashierExeclTypeEnum.NOTE, "辅助核算"),
    HE_ZH_NAME(CashierExeclTypeEnum.NOTE, "对方户名"),
    HE_ZH_CODE(CashierExeclTypeEnum.NOTE, "对方账号"),
    HE_ZH_BANK(CashierExeclTypeEnum.NOTE, "对方银行"),
    BORROW(CashierExeclTypeEnum.NOTE, "收入"),
    LOANS(CashierExeclTypeEnum.NOTE, "支出"),
    REMARK(CashierExeclTypeEnum.NOTE, "备注");

    private CashierExeclTypeEnum key;
    private String value;

    CashierExeclEnum(CashierExeclTypeEnum key, String value) {
        this.key = key;
        this.value = value;
    }

    public CashierExeclTypeEnum getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }


    public static CashierExeclTypeEnum getKeyByValue(String value) {
        for (CashierExeclEnum field : values()) {
            if (field.getValue().equals(value)) {
                return field.getKey();
            }
        }
        throw new IllegalArgumentException("找不到对应的键: " + value);
    }

    public static String getValueByKey(CashierExeclTypeEnum key) {
        for (CashierExeclEnum field : values()) {
            if (field.getKey() == key) {
                return field.getValue();
            }
        }
        throw new IllegalArgumentException("找不到对应的值: " + key);
    }
}