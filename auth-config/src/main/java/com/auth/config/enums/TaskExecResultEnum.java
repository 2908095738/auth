package com.auth.config.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskExecResultEnum {

    SUCCESS(1, "成功"),
    FAIL(0, "失败"),
    ;

    private final Integer execResult;

    private final String desc;

    @Override
    public String toString() {
        return desc;
    }
}
