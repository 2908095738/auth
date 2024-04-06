package com.bbs.chat.util;


import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadLocalUtil {


    private  static final ThreadLocal<AuthUtil.UserAPI.User> userThreadLocal = new ThreadLocal<>();

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     */
    public static void addCurrentUser(AuthUtil.UserAPI.User userVO){
        userThreadLocal.set(userVO);
    }

    /**
     * 获取当前登录用户方法
     */
    public static AuthUtil.UserAPI.User getCurrentUser(){
        return userThreadLocal.get();
    }


    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void remove(){
        userThreadLocal.remove();
    }
}
