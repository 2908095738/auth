package com.bbs.financial.enums;

public enum ChangeItemEnum {


    //原值、折旧方法、使用部门、资产类别、预计使用期限、减值准备、残值率、累计折旧
    ASSET_VALUE("原值", "originalValue"),
    DEPRECIATION_METHOD("折旧方法", "depreciationMethod"),
    USE_DEPARTMENT("使用部门", "structureId"),
    ASSET_CATEGORY("资产类别", "assetTypeId"),
    EXPECTED_USE_PERIOD("预计使用期限", "durableMonths"),
    DEPRECIATION_PREPARATION("减值准备", "impairment"),
    RESIDUAL_VALUE_RATE("残值率","ratioRemaining"),
    ACCUMULATED_DEPRECIATION("期初累计折旧", "beginDepreciationAccumulated"),
    ;

    private String name;
    private String value;

    ChangeItemEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static String getName(String value) {
        for (ChangeItemEnum item : ChangeItemEnum.values()) {
            if (item.getValue().equals(value)) {
                return item.getName();
            }
        }
        return null;
    }

}
