package com.auth.login.strategy;

import com.auth.login.AbstractLoginStrategy;
import com.auth.login.enums.LoginType;
import com.auth.login.param.Param;
import com.auth.login.check.Check;
import com.auth.user.dto.UserDTO;
import org.springframework.stereotype.Component;

import static com.auth.login.check.Check.*;
import static com.auth.user.menu.ResponseCodeEnum.FAILED_LOGIN_PWD_ERROR;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * 手机号 + 密码登录策略
 * 策略模式：登录场景下，使用手机号 + 密码登录
 * @author luchenlin
 */
@Component
public class PasswordLoginStrategy extends AbstractLoginStrategy {

    @Override
    protected String getLoginTypeCode() {
        return LoginType.PASSWORD.getCode();
    }

    @Override
    public void checkParam(Param param) throws IllegalArgumentException {
        phoneAndPasswordFormat(param);
    }

    @Override
    public UserDTO login(Param param) throws IllegalArgumentException {
        UserDTO user = searchUser(param.getPhone());
        // 校验：是否未注册
        isNotRegistered(user);
        // 校验：是否设置密码
        isSetPassword(user);
        // 校验：用户状态是否正常
        stateIsNormal(user);
        // 校验：密码是否正确
        if(Check.verifyPassword(param, user)) {
            return user;
        } else {
            if(loginFailCount.tryIncr(user.getId())) {
                return user;
            } else {
                throw new IllegalArgumentException("登录失败达到次数上限，请稍后再试！");
            }
        }
    }
}