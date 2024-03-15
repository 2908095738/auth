package com.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum APIEnum {
    REQUEST_VERIFY("验证token","/user/verify", "post"),
    REQUEST_REGISTER("注册","/user", "put"),
    REQUEST_LOGIN("登录","/user", "post")
    ;

    private final String name;

    private final String path;

    private final String type;
}
