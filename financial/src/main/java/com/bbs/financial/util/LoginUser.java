package com.bbs.financial.util;


import cn.hutool.extra.spring.SpringUtil;
import com.bbs.api.auth.User;
import com.bbs.exception.NoAccountingSetException;
import com.bbs.financial.enums.RedisKeys;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class LoginUser {



    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     */
    public static void set(User user){
        userThreadLocal.set(user);
    }

    /**
     * 获取当前登录用户方法
     */
    public static User get(){
        return userThreadLocal.get();
    }

    /**
     * 获取当前登录用户方法
     */
    public static Long getId(){
        return userThreadLocal.get().getId();
    }

    public static Long getCompanyId(){
        return userThreadLocal.get().getCompanyId();
    }


    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void remove(){
        userThreadLocal.remove();
    }


    public static Long getLoginSetId(){
        RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
        User user = LoginUser.get();
        Object o = redisUtil.get(RedisKeys.FINANCIAL_USER_SET.key(user.getId() + "_" + user.getCompanyId()));
        if(Objects.isNull(o)){
            //返回需要重新选择账套
            throw new NoAccountingSetException();
        }{
            return Long.valueOf(o.toString());
        }
    }
}
