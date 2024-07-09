package com.bbs.financial.enums;

/**
 * 表格对应数据
 */
public enum CashierExeclTypeEnum {
    NOTE("日记账");

    private String type;

    CashierExeclTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}