package com.auth.cache;

import com.auth.entity.User;
import com.bbs.exception.BusinessException;

public interface UserCache {

    /**
     * 查询用户
     * @param uid 用户 ID
     * @return User
     * @throws InterruptedException 中断【等待其他线程加载用户数据】行为
     * @throws IllegalArgumentException 对应用户不存在
     */
    User search(Long uid) throws InterruptedException, IllegalArgumentException;

    /**
     * 查询用户
     * @param email 用户邮箱（唯一）
     * @return User
     * @throws InterruptedException 中断【等待其他线程加载用户数据】行为
     * @throws IllegalArgumentException 对应用户不存在
     */
    User search(String email) throws InterruptedException, IllegalArgumentException;


    /**
     * 通过 ID 更新
     * @param user User
     */
    void updateByID(User user) throws InterruptedException, BusinessException;
}
