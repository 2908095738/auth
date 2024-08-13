package com.bbs.financial.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发票状态枚举
 */
@Getter
@AllArgsConstructor
public enum InvStatusEnum {

    NORMAL(0, "正常"),
    RED(1, "红冲"),
    FAIL(2, "作废"),
    ERR(3, "异常"),
    LOSE(4, "失控");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    public static final Map<Integer, InvStatusEnum> enumMap =
            EnumUtil.getEnumMap(InvStatusEnum.class)
                    .values().stream()
                    .collect(Collectors.toMap(InvStatusEnum::getCode, item -> item));

    public static InvStatusEnum getEnumByMsg(String msg) {
        for (InvStatusEnum now : values()) {
            if (now.getMsg().equals(msg)) {
                return now;
            }
        }
        throw new IllegalArgumentException("no find enum: " + msg);
    }
}