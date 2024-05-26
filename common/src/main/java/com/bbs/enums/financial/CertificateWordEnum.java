package com.bbs.enums.financial;

import cn.hutool.core.util.EnumUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum CertificateWordEnum {

    RECORD(0, "记");

    @EnumValue
    private final Integer code;

    private final String msg;

    public static final Map<Integer, CertificateWordEnum> enumMap =
            EnumUtil.getEnumMap(CertificateWordEnum.class).values().stream()
                    .collect(Collectors.toMap(CertificateWordEnum::getCode, item -> item));

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        private Integer code;

        private String msg;

        public Item(CertificateWordEnum wordEnum) {
            this.code = wordEnum.code;
            this.msg = wordEnum.msg;
        }
    }
}
