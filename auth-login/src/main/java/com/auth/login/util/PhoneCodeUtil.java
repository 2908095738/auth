package com.auth.login.util;

import cn.hutool.extra.spring.SpringUtil;
import com.auth.phone.code.cache.PhoneCodeCache;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

/**
 * 手机验证码工具类
 */
public class PhoneCodeUtil {

    /**
     * 校验输入验证码与缓存验证码是否一致
     * @param phone 手机号
     * @param inputCode 输入验证码
     * @throws IllegalArgumentException 输入验证码不可用
     */
    public static void checkCodeConsistent(String phone, String inputCode) throws IllegalArgumentException {
        Integer code = SpringUtil.getBean(PhoneCodeCache.class).get(phone);
        checkArgument(nonNull(code) && code.equals(Integer.valueOf(inputCode)), "输入验证码不可用");
    }
}
