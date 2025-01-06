package com.bbs.auth.app.login.util;

import cn.hutool.extra.spring.SpringUtil;
import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.UserService;
import com.bbs.enums.UserStateEnum;
import com.bbs.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import static com.bbs.enums.CodeEnum.FAILED_LOGIN_PWD_ERROR;
import static com.bbs.enums.CodeEnum.FAILED_LOGIN_USER_STATUS_ERROR;
import static com.bbs.util.PhoneUtil.checkPhoneFormatThrows;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * 用户相关工具类
 */
public class UserUtil {

    public static Boolean isNormal(User user) throws IllegalArgumentException {
        checkArgument(UserStateEnum.STATUS_NORMAL.getCode().equals(user.getState()), FAILED_LOGIN_USER_STATUS_ERROR);
        return true;
    }

    public static void checkUserPWD(Param param, User user) throws IllegalArgumentException {
        String encryptPassword = SpringUtil.getBean(UserService.class).encryptPassword(param.getPassword(), user.getSalt());
        checkArgument(user.getPassword().equals(encryptPassword), FAILED_LOGIN_PWD_ERROR);
    }

    public static void checkPhoneAndPWDFormat(Param param) throws IllegalArgumentException {
        checkPhoneFormatThrows(param.getPhone());
        checkArgument(StringUtils.isNoneBlank(param.getPassword()));
    }
}
