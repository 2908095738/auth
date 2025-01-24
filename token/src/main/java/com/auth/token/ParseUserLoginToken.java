package com.auth.token;

import com.auth.token.impl.dto.UserLoginToken;
import com.auth.token.impl.exception.UserTokenParseException;

import javax.servlet.http.HttpServletRequest;

/**
 * 解析 Token
 * @author ext.luchenlin5
 */
public interface ParseUserLoginToken {

    UserLoginToken parse(String token);

    UserLoginToken parse(HttpServletRequest request) throws UserTokenParseException;
}
