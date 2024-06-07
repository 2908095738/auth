package com.bbs.financial.enums;

/**
 * 折旧方法：双倍余额递减法，平均年限法，不折旧
 */
public enum DepreciationMethod {


    AVERAGE_YEARS(0, "平均年限法"),
    DOUBLE_BALANCE_DECREASE(1, "双倍余额递减法"),
    NO_DEPRECIATION(2, "不折旧");

    int key;
    String value;
    DepreciationMethod(int key, String value) {
        this.key = key;
        this.value = value;
    }
    public int getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }

    public static String getValueByKey(int key) {
        for (DepreciationMethod item : DepreciationMethod.values()) {
            if (item.getKey() == key) {
                return item.getValue();
            }
        }
        return null;
    }
}
