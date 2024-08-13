package com.bbs.financial.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 新增-费用小票-计税方式
 */
@Getter
@AllArgsConstructor
public enum TaxTypeEnum {
    NO_TAX(0, "不计税"),

    FLY_PLUS_TAX(1, "(机票+燃油费)/(1+税率)*税率"),
    FLY_TAX(2, "(机票+燃油费)*税率"),

    TOTAL_PLUS_TAX(3, "价税合计/(1+税率)*税率"),
    TOTAL_TAX(4, "价税合计*税率");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    public static final Map<Integer, TaxTypeEnum> enumMap =
            EnumUtil.getEnumMap(TaxTypeEnum.class)
                    .values().stream()
                    .collect(Collectors.toMap(TaxTypeEnum::getCode, item -> item));
}