package com.auth.cache;

import com.auth.entity.User;
import com.auth.entity.UserBind;
import com.auth.entity.VXUser;
import com.clinic.exception.BusinessException;

public interface UserCache {

    void setUser(User user);

    void setUserAndOpenIDMap(UserBind userBind);

    void setUserAndPhoneMap(User user);

    void setUserAndPhoneAndOpenIDMap(User user, String openID);

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
     * 微信小程序用户查询
     * @param openid 微信用户唯一标识
     * @return User
     * @throws InterruptedException 中断【等待其他线程加载用户数据】行为
     * @throws IllegalArgumentException 账号未绑定微信，需要绑定微信后重试
     */
    VXUser searchByOpenID(String openid) throws InterruptedException, IllegalArgumentException;


    /**
     * 通过 ID 更新
     * @param user User
     */
    void updateByID(User user) throws InterruptedException, BusinessException;

    /**
     * 手机号用户查询 or 注册用户
     * @param phone 手机号
     * @return User
     * @throws IllegalArgumentException 对应用户不存在
     */
    User searchOrRegisterByPhone(Long phone) throws InterruptedException, IllegalArgumentException;

    User searchOrRegisterByPhone(String phone) throws InterruptedException, IllegalArgumentException;

    /**
     * 通过手机号查询用户（需要加锁！！！！）
     * @param phone 手机号
     * @return 用户
     */
    User searchByPhoneNoLockNoLoad(String phone) throws InterruptedException;

    Long searchUIDByCache(String phone) throws InterruptedException;
}
