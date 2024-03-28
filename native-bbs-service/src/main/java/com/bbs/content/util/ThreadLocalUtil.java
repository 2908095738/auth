package com.bbs.content.util;


import com.bbs.entity.UserVO;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadLocalUtil {


    private  static final ThreadLocal<UserVO> userThreadLocal = new ThreadLocal<>();

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     */
    public static void addCurrentUser(UserVO userVO){
        userThreadLocal.set(userVO);
    }

    /**
     * 获取当前登录用户方法
     */
    public static UserVO getCurrentUser(){
        return userThreadLocal.get();
    }


    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void remove(){
        userThreadLocal.remove();
    }
}
