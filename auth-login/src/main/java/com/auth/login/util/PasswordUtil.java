package com.auth.login.util;

import cn.hutool.crypto.SecureUtil;

public class PasswordUtil {

    /**
     * 加密密码
     * @param password 密码
     * @param salt 盐（随机字符串）
     * @return 加密后的密码
     */
    public static String encryptPassword(String password, Integer salt) {
        return SecureUtil.md5(password + salt);
    }
}
