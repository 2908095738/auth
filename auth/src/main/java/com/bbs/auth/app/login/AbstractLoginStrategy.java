package com.bbs.auth.app.login;

import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.cache.code.PhoneCodeCache;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.UserService;
import com.bbs.enums.LoginType;

import javax.annotation.Resource;

/**
 * 登录策略抽象类
 * 策略模式：登录场景下，使用不同登录类型实现
 * @author luchenlin
 */
public abstract class AbstractLoginStrategy {

    @Resource
    protected PhoneCodeCache phoneCodeCache;

    @Resource
    protected UserCache userCache;

    @Resource
    protected UserService userService;

    protected abstract LoginType getLoginType();

    protected abstract void checkParam(Param param) throws IllegalArgumentException;

    protected abstract User login(Param param) throws IllegalArgumentException;

    public User tryLogin(Param param) {

        checkParam(param);

        return login(param);
    }
}
