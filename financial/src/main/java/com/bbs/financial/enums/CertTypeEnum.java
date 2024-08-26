package com.bbs.financial.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 凭证类型枚举
 */
@Getter
@AllArgsConstructor
public enum CertTypeEnum {
    //资产凭证
    BUY_ASSET(1, "购入凭证"),
    OLD_ASSET(2, "折旧凭证"),
    LESS_ASSET(3, "减值凭证"),
    CLEAR_ASSET(4, "清理凭证"),
    OTHER_ASSET(5, "其他凭证"),

    ORI_NOTE(6, "初始金额_日记账_凭证"),
    NORMAL_NOTE(7, "普通类型_日记账_凭证"),

    INVOICE(8, "发票凭证"),

    NONE(-1,"Nothing")
    ;

    @EnumValue
    @JsonValue
    private final Integer type;

    private final String msg;

    public static final Map<Integer, CertTypeEnum> enumMap =
            EnumUtil.getEnumMap(CertTypeEnum.class)
                    .values().stream()
                    .collect(Collectors.toMap(CertTypeEnum::getType, item -> item));
}