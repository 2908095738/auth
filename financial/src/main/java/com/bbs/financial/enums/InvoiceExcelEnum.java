package com.bbs.financial.enums;

/**
 * 发票表格实例域枚举
 */
public enum InvoiceExcelEnum {
    INV_CODE("发票代码"),
    INV_NUM("发票号码"),
    ELE_INV_NUM("全电发票号码"),

    SALE_USCC("销方识别号"),
    SALE_NAME("销方名称"),

    BUY_USCC("购方识别号"),
    BUY_NAME("购买方名称"),

    OPEN_DATE("开票日期"),
    DETAIL_NAME("货物或应税劳务名称"),
    DETAIL_CODE("规格型号"),
    UNIT("单位"),

    QUANTITY("数量"),
    PRICE("单价"),
    NON_TAX_MONEY("金额"),
    TAX("税率"),
    TAX_MONEY("税额"),

    INV_TYPE("发票票种"),
    INV_STATUS("发票状态"),
    REMARK("备注");

    private String name;

    InvoiceExcelEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}