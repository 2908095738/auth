package com.auth.user.impl.config.threadlocal;

import com.auth.user.dto.UserDTO;
import com.auth.user.exception.UserNotLoginException;

/**
 * 登录用户 thread local
 * @author ext.luchenlin5
 */
public class LoginUserThreadLocal {

    private static final ThreadLocal<UserDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     */
    public static void set(UserDTO user){
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取当前登录用户方法
     * @throws UserNotLoginException 用户未登录异常
     */
    public static UserDTO get() throws UserNotLoginException {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前登录用户方法
     */
    public static Long getId(){
        return USER_THREAD_LOCAL.get().getId();
    }

    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void remove(){
        USER_THREAD_LOCAL.remove();
    }
}
