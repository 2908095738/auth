package com.auth.login.check;

import com.auth.login.param.Param;
import com.auth.login.util.PasswordUtil;
import com.auth.user.dto.UserDTO;
import com.auth.user.menu.UserStateEnum;
import org.apache.commons.lang3.StringUtils;

import static com.auth.user.menu.ResponseCodeEnum.*;
import static com.auth.user.menu.ResponseCodeEnum.FAILED_LOGIN_USER_NOT_SET_PWD;
import static com.auth.util.PhoneUtil.checkPhoneFormatThrows;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

/**
 * 用户校验
 */
public class Check {

    /**
     * 用户状态是否正常
     */
    public static Boolean stateIsNormal(UserDTO user) throws IllegalArgumentException {
        checkArgument(UserStateEnum.STATUS_NORMAL.getCode().equals(user.getState()), FAILED_LOGIN_USER_STATUS_ERROR);
        return true;
    }

    /**
     * 校验密码格式
     */
    public static Boolean verifyPassword(Param param, UserDTO user) throws IllegalArgumentException {
        String encryptPassword = PasswordUtil.encryptPassword(param.getPassword(), user.getSalt());
        return user.getPassword().equals(encryptPassword);
    }

    /**
     * 校验手机号和密码格式
     */
    public static void phoneAndPasswordFormat(Param param) throws IllegalArgumentException {
        checkPhoneFormatThrows(param.getPhone());
        checkArgument(StringUtils.isNoneBlank(param.getPassword()));
    }

    public static boolean isNotRegistered(UserDTO user) {
        checkArgument(nonNull(user), FAILED_LOGIN_USER_NEED_REGISTER);
        return true;
    }

    public static boolean isSetPassword(UserDTO user) {
        checkArgument(nonNull(user.getPassword()), FAILED_LOGIN_USER_NOT_SET_PWD);
        return true;
    }
}
