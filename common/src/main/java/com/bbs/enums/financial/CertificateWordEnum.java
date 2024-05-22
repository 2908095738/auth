package com.bbs.enums.financial;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CertificateWordEnum {

    RECORD(0, "记");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    @Override
    public String toString() {
        return msg;
    }
}
