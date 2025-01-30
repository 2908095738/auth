package com.auth.login.strategy;

import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.auth.login.AbstractLoginStrategy;
import com.auth.login.config.WxConfig;
import com.auth.login.enums.LoginType;
import com.auth.login.param.Param;
import com.auth.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.auth.login.util.PhoneCodeUtil.checkCodeConsistent;
import static com.auth.util.PhoneUtil.checkPhoneCodeFormat;
import static com.auth.util.PhoneUtil.checkPhoneFormatThrows;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

/**
 * 微信扫码关注公众号登录策略
 * 策略模式：登录场景下，使用微信扫码关注公众号登录
 * @author luchenlin
 */
@Slf4j
@Component
public class WXLoginStrategy extends AbstractLoginStrategy {

    @Value("${wx.oa.token}")
    private String token;

    @Resource
    private WxConfig wxConfig;

    @Override
    protected String getLoginTypeCode() {
        return LoginType.WX.getCode();
    }

    @Override
    public void checkParam(Param param) throws IllegalArgumentException {
        // 1. 校验手机号 & 验证码格式
        checkPhoneFormatThrows(param.getPhone());
        checkPhoneCodeFormat(param.getCode());
    }

    @Override
    public UserDTO login(Param param) throws IllegalArgumentException {
        return login(param.getPhone(), param.getCode(), param.getOpenId());
    }

    private UserDTO login(String phone, String paramCode, String inputOpenId) {
        // 场景1：未注册（手机号未注册，且微信未绑定）
        // 场景2：手机号已注册，但微信未绑定
        // PS：不需要【响应用户未绑定手机号，需要绑定手机号】步骤，已在上个步骤【轮询扫码状态】中判断并响应

        // 2. 查询验证码，并比较
        checkCodeConsistent(phone, paramCode);
        // 3. 删除验证码
        phoneCodeCache.remove(phone);
        // 4. 解码 VXOpenId
        String openId = decryptOpenId(inputOpenId);
        // 5. 根据手机号查询用户
        UserDTO user = searchUser.byPhone(phone);
        if(nonNull(user)) {
            // 存在用户，直接绑定 VX
            editUser.bindVXOpenId(user.getId(), openId);
            user.setOpenId(openId);
            userCache.reload(user);
            //公众号下发绑定成功
            wxConfig.sendBindingMassage(openId, user);
        } else {
            // 不存在则注册用户
            user = new UserDTO(phone, openId);
            saveUser.save(user);
            //公众号下发注册成功
            wxConfig.sendRegisterMassage(openId, user);
        }
        return user;
    }

    private String decryptOpenId(String openId) {
        checkArgument(StringUtils.isNotBlank(openId), "缺少微信用户ID");
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, token.getBytes());
        return new String(aes.decrypt(openId));
    }
}