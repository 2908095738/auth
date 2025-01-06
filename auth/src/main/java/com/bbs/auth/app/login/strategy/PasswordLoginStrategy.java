package com.bbs.auth.app.login.strategy;

import com.bbs.auth.app.login.AbstractLoginStrategy;
import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.entity.User;
import com.bbs.enums.LoginType;
import org.springframework.stereotype.Component;

import static com.bbs.auth.app.login.util.UserUtil.*;
import static com.bbs.enums.CodeEnum.FAILED_LOGIN_USER_NEED_REGISTER;
import static com.bbs.enums.CodeEnum.FAILED_LOGIN_USER_NOT_SET_PWD;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

/**
 * 手机号 + 密码登录策略
 * 策略模式：登录场景下，使用手机号 + 密码登录
 * @author luchenlin
 */
@Component
public class PasswordLoginStrategy extends AbstractLoginStrategy {

    @Override
    protected LoginType getLoginType() {
        return LoginType.PASSWORD;
    }

    @Override
    public void checkParam(Param param) throws IllegalArgumentException {
        checkPhoneAndPWDFormat(param);
    }

    @Override
    public User login(Param param) throws IllegalArgumentException {
        User user = searchUser(param.getPhone());
        if(isNotRegistered(user) && isSetPassword(user) && isNormal(user)) {
            checkUserPWD(param, user);
            return user;
        }
        return null;
    }

    private boolean isNotRegistered(User user) {
        checkArgument(nonNull(user), FAILED_LOGIN_USER_NEED_REGISTER);
        return true;
    }

    private boolean isSetPassword(User user) {
        checkArgument(nonNull(user.getPassword()), FAILED_LOGIN_USER_NOT_SET_PWD);
        return true;
    }

    private User searchUser(String phone) {
        return userCache.searchByPhoneNoLockNoLoad(phone);
    }
}