package com.bbs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum CodeEnum {

    FAILED_BUSINESS(401, "业务异常"),

    FAILED_PARAM_NOT_AVAILABLE(400, "参数不可用"),

    FAILED_USER_INFO_DUPLICATION(400, "用户信息重复"),

    FAILED_USER_NOT_LOGIN(401, "用户未登录"),

    SUCCESS_USER_LOGIN(200,"用户登录成功"),

    FAILED_USER_LOGIN_EXPIRE(401, "请重新登录！");


    private final Integer code;

    private final String msg;
}
