package com.auth.service;

import com.auth.entity.User;
import com.auth.entity.UserVO;
import com.auth.entity.VXUser;
import com.clinic.exception.ReLoginException;

import javax.servlet.http.HttpServletRequest;

public interface TokenService {

    String getTokenKey(Long uid);

    UserVO verify(String token) throws ReLoginException;

    Boolean verifyToken(String token);

    UserVO parseToken(String token);

    void extendLoginTime(UserVO user);

    void extendLoginTime(User user);

    String createToken(User user);

    String createToken(VXUser user);

    /**
     * 设置登录标识
     * @param uid 用户 ID（主键）
     */
    void setLoginFlag(Long uid);

    String getLoginFlag(Long uid);

    /**
     * 清除登录标识
     * @param uid 用户 ID（主键）
     */
    void clearLoginFlag(Long uid);

    String getToken(HttpServletRequest request);
}
