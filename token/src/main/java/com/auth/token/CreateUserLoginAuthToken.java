package com.auth.token;

import com.auth.token.impl.dto.UserLoginToken;

/**
 * 登录验证 Token 操作
 * @author ext.luchenlin5
 */
public interface CreateUserLoginAuthToken {

    String create(Long userId, String userName, Long timeout);

    String create(UserLoginToken userLoginToken);
}