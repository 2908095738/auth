package com.clinic.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockStateEnum {

    UNDEFINED(-1, "未定义"),
    NORMAL(0, "正常"),
    SHORTAGE(1, "短缺");

    private Integer code;

    private String msg;
}
