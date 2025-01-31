package com.auth.token.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.http.Header;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import com.auth.config.Config;
import com.auth.config.impl.entity.SystemConfigItem;
import com.auth.token.Token;
import com.auth.token.impl.dto.UserLoginToken;
import com.auth.token.impl.exception.UserTokenParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Primary
@Service
public class TokenServiceImpl implements Token.CreateUserLoginAuthToken, Token.VerifyUserLoginAuthToken, Token.ParseUserLoginToken, InitializingBean {

    private static SystemConfigItem tokenKeyConfig;

    private static final String TOKEN_KEY_CONFIG_CODE = "login_token_key";

    private static SystemConfigItem tokenTimeoutDayConfig;

    private static final String TOKEN_TIMEOUT_CONFIG_CODE = "login_token_timeout_day";

    /**
     * Auth 2.0 Token 中 JWT 认证前缀
     */
    private static final String AUTH2_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT Token Payload 中用户 ID 键 key
     */
    private static final String TOKEN_PAYLOAD_UID_KEY = "uid";

    @Resource
    private Config.SystemConfig systemConfig;

    @Override
    public void afterPropertiesSet() {
        tokenKeyConfig = systemConfig.getConfig(TOKEN_KEY_CONFIG_CODE);
        tokenTimeoutDayConfig = systemConfig.getConfig(TOKEN_TIMEOUT_CONFIG_CODE);
    }

    @Override
    public UserLoginToken create(Long userId) {
        Date now = new Date();
        DateTime expireDate = DateUtil.offsetDay(now, tokenTimeoutDayConfig.getIntValue());
        String token = AUTH2_TOKEN_PREFIX + JWT.create()
                .setIssuedAt(now)   //设置签发时间
                .setExpiresAt(expireDate)   //设置过期时间
                .setPayload(TOKEN_PAYLOAD_UID_KEY, userId)
                .setKey(tokenKeyConfig.getValue().getBytes())   //设置密钥
                .sign();
        return new UserLoginToken(userId, now, expireDate, expireDate, token);
    }

    @Override
    public UserLoginToken parse(String token) throws UserTokenParseException {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            JWTPayload payload = jwt.getPayload();
            JSONObject claimsJson = payload.getClaimsJson();
            Long userId = claimsJson.getBean(TOKEN_PAYLOAD_UID_KEY, Long.class);
            Date issuedAt = claimsJson.getDate(JWTPayload.ISSUED_AT);
            Date notBefore = claimsJson.getDate(JWTPayload.NOT_BEFORE);
            Date expiresAt = claimsJson.getDate(JWTPayload.EXPIRES_AT);
            return new UserLoginToken(userId, issuedAt, notBefore, expiresAt);
        } catch (Exception e) {
            throw new UserTokenParseException();
        }
    }

    @Override
    public UserLoginToken parse(HttpServletRequest request) throws UserTokenParseException {
        String token = request.getHeader(Header.AUTHORIZATION.getValue());
        return parse(token);
    }

    @Override
    public Boolean verify(String inputToken) throws UserTokenParseException {
        String token = inputToken;
        try {
            // 校验：token 字符串是否为空
            checkArgument(isNotBlank(token), "Token 字符串为空");
            // 如果 token 包含 Bearer，则移除
            token = tryDeleteAuth2TokenPrefix(token);
            // 校验：token 签名
            checkArgument(JWTUtil.verify(token, tokenKeyConfig.getValue().getBytes()), "Token 签名校验异常");
            // 校验：token 签发时间与过期时间
            // @see hutool JWT文档 https://doc.hutool.cn/pages/JWTValidator/#%E9%AA%8C%E8%AF%81%E6%97%B6%E9%97%B4
            JWTValidator.of(token).validateDate(DateUtil.date());
            return true;
        } catch (ValidateException e) {
            if(token.startsWith(AUTH2_TOKEN_PREFIX)) {
                token = token.replace(AUTH2_TOKEN_PREFIX, "");
            }
            JWT jwt = JWTUtil.parseToken(token);
            JWTPayload payload = jwt.getPayload();
            JSONObject claimsJson = payload.getClaimsJson();
            Date issuedAt = claimsJson.getDate(JWTPayload.ISSUED_AT);
            Date notBefore = claimsJson.getDate(JWTPayload.NOT_BEFORE);
            Date expiresAt = claimsJson.getDate(JWTPayload.EXPIRES_AT);
            log.info(
                    "用户 Token 校验失败：签发时间与过期时间不一致！ 签发时间={}; 生效时间={}; 过期时间={}; token={}",
                    DateUtil.formatDateTime(issuedAt), DateUtil.formatDateTime(notBefore), DateUtil.formatDateTime(expiresAt), token
            );
            throw new UserTokenParseException();
        } catch (IllegalArgumentException e) {
            log.info("用户 Token 校验失败：{}！", e.getMessage());
            throw new UserTokenParseException();
        } catch (Exception e) {
            log.info("用户 Token 校验失败：{}！ token={};", e.getMessage(), token);
            throw new UserTokenParseException();
        }
    }

    private String tryDeleteAuth2TokenPrefix(String token) {
        // 如果 token 包含 Bearer，则移除
        String result = token;
        if(token.startsWith(AUTH2_TOKEN_PREFIX)) {
            result = token.replace(AUTH2_TOKEN_PREFIX, "");
        }
        return result;
    }

}
