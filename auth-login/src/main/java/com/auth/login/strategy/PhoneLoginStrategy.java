package com.auth.login.strategy;

import com.auth.login.check.Check;
import com.auth.login.enums.LoginType;
import com.auth.login.AbstractLoginStrategy;
import com.auth.login.param.Param;
import com.auth.user.dto.UserDTO;
import org.springframework.stereotype.Component;

import static com.auth.login.util.PhoneCodeUtil.checkCodeConsistent;
import static com.auth.util.PhoneUtil.checkCodeFormat;
import static com.auth.util.PhoneUtil.checkPhoneFormatThrows;
import static java.util.Objects.nonNull;

/**
 * 手机号登录策略
 * 策略模式：登录场景下，使用手机号验证码登录
 * @author luchenlin
 */
@Component
public class PhoneLoginStrategy extends AbstractLoginStrategy {

    @Override
    protected String getLoginTypeCode() {
        return LoginType.PHONE.getCode();
    }

    @Override
    public void checkParam(Param param) throws IllegalArgumentException {
        String phone = param.getPhone();
        String paramCode = param.getCode();

        // 校验：手机号格式
        checkPhoneFormatThrows(phone);
        // 校验：验证码格式
        checkCodeFormat(paramCode);
    }

    @Override
    public UserDTO login(Param param) throws IllegalArgumentException {
        return login(param.getPhone(), param);
    }

    private UserDTO login(String phone, Param param) {
        UserDTO user = searchUser(phone);

        try {
            // 校验：验证码是否一致性
            checkCodeConsistent(param.getPhone(), param.getCode());

            removeCode(param);
            // 用户已注册 > 校验状态 > 返回用户信息
            if(isRegister(user)) {
                Check.stateIsNormal(user);
                // 用户未注册 > 注册用户 > 返回用户信息
            } else {
                user = new UserDTO(phone);
                register(user);
            }
            return user;
        } catch (IllegalArgumentException e) {
            if(loginFailCount.tryIncr(user.getId())) {
                return user;
            } else {
                throw new IllegalArgumentException("登录失败！次数达到次数上限，请稍后再试！");
            }
        }
    }

    private void register(UserDTO user) {
        saveUser.save(user);
    }

    private boolean isRegister(UserDTO user) {
        return nonNull(user);
    }

    /**
     * 删除缓存中的验证码
     * @param phone 手机号
     */
    private void removeCode(String phone) {
        phoneCodeCache.remove(phone);
    }

    private void removeCode(Param param) {
        removeCode(param.getPhone());
    }
}