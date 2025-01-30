package com.auth.token;

import com.auth.token.impl.dto.UserLoginToken;
import com.auth.token.impl.exception.UserTokenParseException;

import javax.servlet.http.HttpServletRequest;

public interface Token {

    interface CreateUserLoginAuthToken {

        UserLoginToken create(Long userId);
    }

    /**
     * 验证 Token
     */
    interface VerifyUserLoginAuthToken {

        Boolean verify(String token) throws UserTokenParseException;
    }

    /**
     * 解析 Token
     */
    interface ParseUserLoginToken {

        UserLoginToken parse(String token);

        UserLoginToken parse(HttpServletRequest request) throws UserTokenParseException;
    }
}
