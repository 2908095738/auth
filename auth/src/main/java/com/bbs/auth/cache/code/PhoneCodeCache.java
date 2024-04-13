package com.bbs.auth.cache.code;

import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.util.ZKUtil;
import com.bbs.auth.enums.ZookeeperNodePaths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.bbs.auth.enums.RedisKeys.USER_PHONE_CODE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Component
public class PhoneCodeCache {

    @Resource
    protected RedisUtil redisUtil;

    @Resource
    protected ZKUtil zkUtil;

    public Integer timeout() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.Captcha.CODE_TIMEOUT);
    }

    public Integer getCode(Long phone) { return getCode(String.valueOf(phone)); }

    public Integer getCode(String phone) {
        String key = key(phone);
        String codeStr = redisUtil.get(key);
        return nonNull(codeStr) ? Integer.valueOf(codeStr) : null;
    }

    public Boolean checkCode(Long phone, Integer code) {
        if(code > 999 && code <= 9999) {
            Integer serverPhoneCode = getCode(phone);
            return nonNull(serverPhoneCode) && serverPhoneCode.equals(code);
        }
        return false;
    }

    /**
     * 清除所有该手机号申请的验证码
     * @param phone 手机号
     */
    public void delCode(String phone) {
        String key = key(phone);
        redisUtil.delete(key);
    }

    /**
     * 设置验证码（同个手机号，重复设置新码，会覆盖旧码）
     * @param phone 手机号
     * @param code 验证码
     */
    public void setCode(String phone, Integer code) {
        String key = key(phone);
        redisUtil.set(key, String.valueOf(code), timeout(), MINUTES);
    }

    public boolean notExists(String phone) {
        return isNull(getCode(phone));
    }

    private String key(String phone) {
        return USER_PHONE_CODE.key(phone);
    }
}
