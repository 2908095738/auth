package com.bbs.financial.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.bbs.financial.enums.TaxBurdenCalTypeEnum.*;

/**
 * 税负测算项目枚举(获取在表中相应类型下的排序)
 */
@Getter
@AllArgsConstructor
public enum TaxBurdenCalItemEnum {
    //销项类型相关
    SALE_NORMAL(SALE, 1, "本月正常开具发票"),
    SALE_RED(SALE, 2, "本月开具红字发票"),
    SALE_ADD_NO(SALE, 3, "加: 未开票收入"),
    SALE_SUB(SALE, 4, "减: 服务、不动产和无形资产扣除项目本期实际扣除额"),
    SALE_ADD_OTHER(SALE, 5, "加: 其他"),
    SALE_TOTAL(SALE, 6, "销项税额小计"),

    //进项类型相关
    IN_AUTH(IN, 1, "本月认证发票"),

    IN_ADD_INV(IN, 2, "加: 其他可抵扣发票"),

    IN_SUB_OUT(IN, 3, "减: 进项转出"),

    IN_ADD_TAX(IN, 4, "加: 其他可抵扣进项税"),

    IN_SUB_AUTH(IN, 5, "减: 认证相符但未申报抵扣"),
    IN_TOTAL(IN, 6, "本期可抵扣进项小计"),

    //应交增值税相关
    VAT_ORI(VAT, 1, "期初留抵"),

    VAT_SUB(VAT, 2, "预计减免税"),

    VAT_DEVICE_NUM(VAT, 3, "增值税税控系统专用设备和技术维护费用抵减"),

    VAT_TAX_NUM(VAT, 4, "加计抵减税额"),

    VAT_3_INPUT(VAT, 5, "INPUT_1"),

    VAT_4_INPUT(VAT, 6, "INPUT_2"),

    VAT_5_INPUT(VAT, 7, "INPUT_3"),

    VAT_MAYBE_TAX(VAT, 8, "本期预计应交税额"),

    VAT_MAYBE_PLUS_TAX(VAT, 9, "本期预计增值税税负率"),

    VAT_END(VAT, 10, "期末留抵税额"),

    //附加税相关
    PLUS_CITY_NUM(ADD_TAX, 1, "城市维护建设税(INPUT_NUM)%"),

    PLUS_EDU_NUM(ADD_TAX, 2, "教育费附加(INPUT_NUM)%"),

    PLUS_AREA_NUM(ADD_TAX, 3, "地方教育费附加(INPUT_NUM)%"),

    PLUS_TOTAL(ADD_TAX, 4, "附加税小计");

    @EnumValue
    private final TaxBurdenCalTypeEnum type;

    @JsonValue
    private final Integer sort;

    @JsonValue
    private final String msg;
}