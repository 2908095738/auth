package com.bbs.util.captcha;

import com.bbs.enums.ZookeeperNodePaths;
import com.bbs.util.RedisUtil;
import com.bbs.util.ZKUtil;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * 验证码工具
 */
public abstract class CaptchaUtil {

    @Resource
    private ZKUtil zkUtil;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 发送短信
     * @param phoneNumber 目标手机号
     * @param signName  签名名称
     * @param templateCode 模板 Code
     * @param templateParam 模板变量实际值（JSON）
     * @throws Exception 发送异常
     */
    public abstract void send(
            String phoneNumber,
            String signName,
            String templateCode,
            String templateParam
    ) throws Exception;

    public Boolean send(String phone) throws IllegalArgumentException {
        checkPhoneFormat(phone);
        Integer code = createCode();
        try {
            send(phone, signName(), templateCode(),  "{" + code + ":\"1234\"}");
            redisUtil.set(phone, code, timeout(), MINUTES);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String signName() {
        return zkUtil.getForPath(ZookeeperNodePaths.Captcha.Alibaba.SIGN_NAME);
    }

    private String templateCode() {
        return zkUtil.getForPath(ZookeeperNodePaths.Captcha.Alibaba.TEMPLATE_CODE);
    }

    private Integer timeout() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.Captcha.CODE_TIMEOUT);
    }

    private Integer createCode() {
        return (int)((Math.random() *9 +1) *1000);
    }

    private void checkPhoneFormat(String phone) throws IllegalArgumentException {
        Preconditions.checkArgument(StringUtils.isNotBlank(phone) && phone.length() == 11, "手机号格式异常");
        for (int i = 0; i < phone.length(); i++) {
            if (phone.charAt(i) == ' ' || (phone.charAt(i) >= 97 &&
                    phone.charAt(i) <= 122) || (phone.charAt(i) >= 65 && phone.charAt(i) <= 90)) {
                throw new IllegalArgumentException("手机号格式异常");
            }
        }
    }
}
