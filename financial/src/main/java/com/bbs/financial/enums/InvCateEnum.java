package com.bbs.financial.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发票分类枚举
 */
@Getter
@AllArgsConstructor
public enum InvCateEnum {
    OUT(0, "销项发票"),
    IN(1, "进项发票"),
    FEES(2, "费用小票");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    public static final Map<Integer, InvCateEnum> enumMap =
            EnumUtil.getEnumMap(InvCateEnum.class)
                    .values().stream()
                    .collect(Collectors.toMap(InvCateEnum::getCode, item -> item));
}