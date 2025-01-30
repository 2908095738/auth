package com.auth.config.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfigStateEnum {

    OPEN(1, "开启"),
    CLOSE(0, "关闭"),
    ;

    private final Integer state;

    private final String desc;

    @Override
    public String toString() {
        return state.toString();
    }
}
