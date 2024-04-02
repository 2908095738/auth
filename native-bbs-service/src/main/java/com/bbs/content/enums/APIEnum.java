package com.bbs.content.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum APIEnum {
    REQUEST_VERIFY("验证token","/system/user/token", "get"),


    ;

    private final String name;

    private final String path;

    private final String type;
}
