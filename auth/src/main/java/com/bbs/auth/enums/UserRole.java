package com.bbs.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    SALES(4, "销售"),
    ;

    private final Integer code;

    private final String description;
}
