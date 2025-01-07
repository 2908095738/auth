package com.bbs.auth.app.login;

import cn.hutool.extra.spring.SpringUtil;
import com.bbs.auth.app.login.param.Param;
import com.bbs.auth.cache.code.PhoneCodeCache;
import com.bbs.auth.cache.user.UserCache;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.UserService;
import com.bbs.enums.LoginType;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录策略抽象类
 * 策略模式：登录场景下，使用不同登录类型实现
 * @author luchenlin
 */
public abstract class AbstractLoginStrategy {

    private static final Map<String, AbstractLoginStrategy> loginStrategyMapping = SpringUtil
            .getBean(ApplicationContext.class).getBeansOfType(AbstractLoginStrategy.class).values()
            .stream().collect(Collectors.toMap(type -> type.getLoginType().getCode(), type -> type));

    @Resource
    protected PhoneCodeCache phoneCodeCache;

    @Resource
    protected UserCache userCache;

    @Resource
    protected UserService userService;

    /**
     * 获取登录类型对应的登录策略
     * @param loginTypeCode 登录类型编码
     * @return 登录策略
     */
    public static AbstractLoginStrategy getInstance(String loginTypeCode) {
        return loginStrategyMapping.get(loginTypeCode);
    }


    protected abstract LoginType getLoginType();

    protected abstract void checkParam(Param param) throws IllegalArgumentException;

    protected abstract User login(Param param) throws IllegalArgumentException;

    public User tryLogin(Param param) {

        checkParam(param);

        return login(param);
    }
}
