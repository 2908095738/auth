package com.bbs.financial.enums;

public enum Status {

    ACTIVE(1, "正常"),
    INACTIVE(0, "禁用");

    private final Integer code;
    private final String desc;

    Status(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
