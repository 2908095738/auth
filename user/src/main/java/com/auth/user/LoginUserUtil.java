package com.auth.user;

import com.auth.user.impl.config.threadlocal.LoginUserThreadLocal;
import com.auth.user.dto.UserDTO;

import static java.util.Objects.nonNull;

/**
 * 登录用户工具类
 * @author ext.luchenlin5
 */
public class LoginUserUtil {

    public static UserDTO tryGet() {
        return LoginUserThreadLocal.get();
    }

    public static Long tryGetId() {
        UserDTO userDTO = LoginUserThreadLocal.get();
        return nonNull(userDTO) ? userDTO.getId() : null;
    }
}
