package com.auth.rbac.enums;

import com.auth.enums.Dict;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Dict
@Getter
@AllArgsConstructor
public enum SystemState {

    NORMAL(0, "正常"),
    ERROR(1, "隐藏（page 类型资源）")
    ;

    private final int code;

    private final String desc;
}
