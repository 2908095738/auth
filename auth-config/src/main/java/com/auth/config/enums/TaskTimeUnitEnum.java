package com.auth.config.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum TaskTimeUnitEnum {

    MILLISECONDS(0, TimeUnit.MILLISECONDS, "毫秒"),
    SEC(1, TimeUnit.SECONDS, "秒"),
    MIN(2, TimeUnit.MINUTES, "分钟"),
    HOUR(3, TimeUnit.HOURS, "小时"),
    DAY(4, TimeUnit.DAYS, "天"),
    ;

    private final Integer code;

    private final TimeUnit unit;

    private final String desc;
}
