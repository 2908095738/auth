package com.auth.token.impl.exception;

/**
 * Token 解析异常
 * @author ext.luchenlin5
 */
public class UserTokenParseException extends RuntimeException {

    public UserTokenParseException() {
        super("登录 Token 解析异常");
    }
}
