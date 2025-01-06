package com.bbs.auth.app.login.strategy;

import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.bbs.auth.app.login.AbstractLoginStrategy;
import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.entity.User;
import com.bbs.auth.util.WxUtil;
import com.bbs.enums.LoginType;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.bbs.auth.app.login.util.UserUtil.*;
import static com.bbs.enums.CodeEnum.*;
import static com.bbs.util.PhoneUtil.checkPhoneCodeFormat;
import static com.bbs.util.PhoneUtil.checkPhoneFormatThrows;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

/**
 * 微信扫码关注公众号登录策略
 * 策略模式：登录场景下，使用微信扫码关注公众号登录
 * @author luchenlin
 */
@Component
public class WXLoginStrategy extends AbstractLoginStrategy {

    @Value("${wx.oa.token}")
    private String token;

    @Resource
    private WxUtil wxUtil;

    @Override
    protected LoginType getLoginType() {
        return LoginType.WX;
    }

    @Override
    public void checkParam(Param param) throws IllegalArgumentException {
        // 1. 校验手机号 & 验证码格式
        checkPhoneFormatThrows(param.getPhone());
        checkPhoneCodeFormat(param.getCode());
    }

    @Override
    public User login(Param param) throws IllegalArgumentException {
        return login(param.getPhone(), param.getCode(), param.getOpenId());
    }

    private User login(String phone, String paramCode, String inputOpenId) {
        // 场景1：未注册（手机号未注册，且微信未绑定）
        // 场景2：手机号已注册，但微信未绑定
        // PS：不需要【响应用户未绑定手机号，需要绑定手机号】步骤，已在上个步骤【轮询扫码状态】中判断并响应

        // 2. 查询验证码，并比较
        Integer code = phoneCodeCache.getCode(phone);
        checkArgument(nonNull(code) && code.equals(Integer.valueOf(paramCode)), FAILED_AUTH_PHONE_CODE_NOT_AVAILABLE);
        // 3. 删除验证码
        phoneCodeCache.delCode(phone);
        // 4. 解码 VXOpenId
        String openId = decryptOpenId(inputOpenId);
        // 5. 根据手机号查询用户
        User user = userCache.searchByPhoneNoLockNoLoad(phone);
        if(nonNull(user)) {
            // 存在用户，直接绑定 VX
            userService.bindUser(user.getId(), openId);
            //公众号下发绑定成功
            wxUtil.sendBindingMassage(openId, user);
        } else {
            // 不存在则注册用户
            user = new User(phone, Long.valueOf(phone), openId);
            userService.save(user);
            //公众号下发注册成功
            wxUtil.sendRegisterMassage(openId, user);
        }
        return user;
    }

    private String decryptOpenId(String openId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(openId), "缺少微信用户ID");
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, token.getBytes());
        return new String(aes.decrypt(openId));
    }
}