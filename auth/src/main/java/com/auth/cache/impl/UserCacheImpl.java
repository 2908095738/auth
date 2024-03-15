package com.auth.cache.impl;

import cn.hutool.json.JSONUtil;
import com.auth.cache.UserCache;
import com.auth.entity.User;
import com.auth.service.UserService;
import com.auth.util.RedisUtil;
import com.clinic.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.auth.enums.RedisKeys.USER;
import static com.auth.enums.RedisKeys.USER_EMAIL_AND_ID_MAP;
import static com.auth.util.RedisUtil.Redisson.*;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


@Slf4j
@Component
public class UserCacheImpl implements UserCache {


    @Resource
    private RedisUtil redis;

    @Resource
    private RedissonClient redisson;

    private final UserService service;

    @Override
    public User search(Long uid) throws InterruptedException, IllegalArgumentException {
        User user = redis.get(USER.key(uid), User.class);

        if(nonNull(user)) {
            return user;

        } else {
            return lockExec(redisson.getSpinLock(USER.LOCK.key(uid)), 500, 1000, MILLISECONDS, () ->
                    load(uid, 7, DAYS)
            );
        }
    }

    @Override
    public User search(String email) throws InterruptedException, IllegalArgumentException {
        RLock lock = redisson.getSpinLock(USER.LOCK.key(email));


        String uid = searchUidFromRedis(email);


        if(isNotBlank(uid)) {
            // 有 email，无用户信息
            return lockExec(lock, 500, 1000, MILLISECONDS, () ->
                    load(Long.valueOf(uid), 7, DAYS)
            );

        } else {
            // 都没有
            return lockExec(lock, 500, 1000, MILLISECONDS, () -> {
                User user = searchUserFromDBElseThrow(email);
                setUserAndEmailMapToRedis(user);
                return user;
            });
        }
    }

    private String searchUidFromRedis(String email) {
        return redis.getOpt(USER_EMAIL_AND_ID_MAP.key(email)).get();
    }

    private void setUserAndEmailMapToRedis(User user) {
        redis.multiSet(new HashMap<String, String>() {{
            put(USER_EMAIL_AND_ID_MAP.key(user.getEmail()), String.valueOf(user.getId()));
            put(USER.key(user.getId()), JSONUtil.toJsonPrettyStr(user));
        }});
    }

    private User searchUserFromDBElseThrow(String email) {
        return service.searchElseThrow(email);
    }

    @Override
    public void updateByID(User user) throws BusinessException {
        Long uid = user.getId();

        lockExec(redisson.getSpinLock(USER.LOCK.key(uid)), 500, 1000, MILLISECONDS, () -> {
            if(service.updateById(user)) {
                load(service.getById(uid), 7, DAYS);
                return;
            }
            throw new BusinessException("修改用户密码失败！");
        });
    }

    /**
     * 加载用户信息到缓存
     * @param uid 用户 ID（主键）
     * @param timeout 超时时间
     * @param unit 超时时间单位
     * @return 用户信息
     * @throws IllegalArgumentException 对应 ID 用户不存在！
     */
    private User load(Long uid, int timeout, TimeUnit unit) throws IllegalArgumentException {
        User user = service.getOptById(uid).orElseThrow(() -> new IllegalArgumentException("对应 ID 用户不存在！"));
        redis.set(USER.key(uid), user, timeout, unit);
        return user;
    }

    private void load(User user, int timeout, TimeUnit unit) {
        redis.set(USER.key(user.getId()), user, timeout, unit);
    }
    @Autowired
    public UserCacheImpl(UserService service) {
        this.service = service;
    }
}
