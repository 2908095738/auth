package com.bbs.financial.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 科目摘要
 */
@Getter
@AllArgsConstructor
public enum AccountAbstractEnum {

    BEGINNING_BALANCE("年初余额"),

    OPENING_BALANCE("期初余额"),

    CURRENT_TOTAL("本期合计"),

    CURRENT_YEAR_CUMULATIVE("本年累计");

    private final String name;
}
