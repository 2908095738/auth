package com.auth.user;

import com.auth.user.dto.UserDTO;

/**
 * 基础 Controller
 * 用于业务 Controller 继承该类，快速研发
 * @author ext.luchenlin5
 */
public abstract class BaseController {

    protected UserDTO loginUser() {
        return User.LoginUserUtil.tryGet();
    }

    protected Long loginUserId() {
        return User.LoginUserUtil.tryGetId();
    }
}
