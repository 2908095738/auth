package com.bbs.auth.app.login.strategy;

import com.bbs.auth.app.login.AbstractLoginStrategy;
import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.entity.User;
import com.bbs.enums.LoginType;
import org.springframework.stereotype.Component;

import static com.bbs.auth.app.login.util.PhoneCodeUtil.checkCodeConsistent;
import static com.bbs.auth.app.login.util.UserUtil.isNormal;
import static com.bbs.util.PhoneUtil.checkCodeFormat;
import static com.bbs.util.PhoneUtil.checkPhoneFormatThrows;
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

        checkPhoneFormatThrows(phone);
        checkCodeFormat(paramCode);
        checkCodeConsistent(param);
    }

    @Override
    public User login(Param param) throws IllegalArgumentException {
        return login(param.getPhone(), param);
    }

    private User login(String phone, Param param) {
        removeCode(param);

        User user = searchUser(phone);

        // 用户已注册 > 校验状态 > 返回用户信息
        if(isRegister(user)) {
            isNormal(user);
            // 用户未注册 > 注册用户 > 返回用户信息
        } else {
            user = new User(phone, Long.valueOf(phone));
            register(user);
        }
        return user;
    }

    private void register(User user) {
        userService.save(user);
    }

    private boolean isRegister(User user) {
        return nonNull(user);
    }

    /**
     * 删除缓存中的验证码
     * @param phone 手机号
     */
    private void removeCode(String phone) {
        phoneCodeCache.delCode(phone);
    }

    private void removeCode(Param param) {
        removeCode(param.getPhone());
    }

    private User searchUser(String phone) {
        return userCache.searchByPhoneNoLockNoLoad(phone);
    }
}