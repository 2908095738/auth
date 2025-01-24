package com.auth.user.exception;

/**
 * 用户未登录异常
 * @author ext.luchenlin5
 */
public class UserNotLoginException extends RuntimeException {

    public UserNotLoginException() {
        super("用户未登录");
    }
}
