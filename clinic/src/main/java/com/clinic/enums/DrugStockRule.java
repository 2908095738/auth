package com.clinic.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DrugStockRule {

    MIN_UNIT_PERCENTAGE_CUSTOMIZE(1, "自定义（最小单位）", "当库存数量不足【自定义阈值】时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）"),
    MIN_UNIT_PERCENTAGE_80(2, "80%（最小单位）", "当库存数量不足 80% 时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）"),
    MIN_UNIT_PERCENTAGE_50(3, "50%（最小单位）", "当库存数量不足 50% 时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）"),
    MIN_UNIT_PERCENTAGE_20(4, "20%（最小单位）", "当库存数量不足 50% 时，提醒库存不足（库存数量按最小单位统计，如粒/片/袋等）");

    private Integer code;

    private String msg;

    private String remark;

    public static List<DrugStockRule> list = Arrays.asList(
            MIN_UNIT_PERCENTAGE_CUSTOMIZE,
            MIN_UNIT_PERCENTAGE_80,
            MIN_UNIT_PERCENTAGE_50,
            MIN_UNIT_PERCENTAGE_20
    );
}
