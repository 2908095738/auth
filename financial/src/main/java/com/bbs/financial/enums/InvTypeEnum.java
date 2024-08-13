package com.bbs.financial.enums;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发票类型枚举
 */
@Getter
@AllArgsConstructor
public enum InvTypeEnum {

    PAPER_NORMAL(1, "纸质普票"),
    PAPER_S(2, "纸质专票"),
    ELE_S(3, "电子专票"),
    ELE_NORMAL(4, "电子普票"),
    CAR(5, "机动车票"),
    TWO(6, "二手车票"),
    PASS(7, "通行费票"),

    //费用小票
    FLY(11, "飞机票"),
    TRAIN(12, "火车票"),
    BUS(13, "公路客运票"),
    WATER(14, "水路客运费"),
    ROAD(15, "过桥过路费票"),
    TAXI(16, "出租车票"),
    GE(17, "通用机打发票"),
    SET(18, "定额发票"),
    OTHER(19, "其他票"),
    OCEAN(20, "海关缴款书"),
    BACK(21, "火车票退票凭证"),
    FISCAL(22, "财政电子票据");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    public static final Map<Integer, InvTypeEnum> enumMap =
            EnumUtil.getEnumMap(InvTypeEnum.class)
                    .values().stream()
                    .collect(Collectors.toMap(InvTypeEnum::getCode, item -> item));

    public static InvTypeEnum getEnumByMsg(String msg) {
        for (InvTypeEnum now : values()) {
            if (now.getMsg().equals(msg)) {
                return now;
            }
        }
        throw new IllegalArgumentException("no find enum: " + msg);
    }
}