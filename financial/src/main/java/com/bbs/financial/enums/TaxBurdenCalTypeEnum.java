package com.bbs.financial.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 税负测算类型枚举
 */
@Getter
@AllArgsConstructor
public enum TaxBurdenCalTypeEnum {
    SALE(1, "销项"),

    IN(2, "进项"),

    VAT(3, "应交增值税"),

    ADD_TAX(4, "附加税");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    public static final Map<Integer, TaxBurdenCalTypeEnum> enumMap =
            EnumUtil.getEnumMap(TaxBurdenCalTypeEnum.class)
                    .values().stream()
                    .collect(Collectors.toMap(TaxBurdenCalTypeEnum::getCode, item -> item));
}