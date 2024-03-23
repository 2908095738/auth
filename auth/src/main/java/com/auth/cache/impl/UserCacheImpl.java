package com.auth.cache.impl;

import cn.hutool.core.util.RandomUtil;
import com.auth.cache.UserCache;
import com.auth.dao.UserDao;
import com.auth.entity.User;
import com.auth.entity.UserBind;
import com.auth.entity.VXUser;
import com.auth.enums.ZookeeperNodePaths;
import com.auth.service.UserService;
import com.auth.util.RedisUtil;
import com.auth.util.ZKUtil;
import com.bbs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static cn.hutool.json.JSONUtil.toJsonPrettyStr;
import static com.auth.enums.RedisKeys.*;
import static com.auth.util.RedisUtil.Redisson.*;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.*;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


@Slf4j
@Component
public class UserCacheImpl implements UserCache {


    @Resource
    private RedisUtil redis;

    @Resource
    private RedisUtil.Redisson redissonUtil;

    @Resource
    private RedissonClient redisson;

    @Resource
    private ZKUtil zkUtil;

    private final UserService service;

    private final UserDao dao;

    public String getFilterKey() {
        return zkUtil.getForPath(ZookeeperNodePaths.CacheConf.User.FILTER_KEY);
    }

    public Long getExpectedInsertions() {
        return zkUtil.getLongForPath(ZookeeperNodePaths.CacheConf.User.FILTER_EXPECTED_INSERTIONS);
    }

    public Double getFalseProbability() {
        return zkUtil.getDoubleForPath(ZookeeperNodePaths.CacheConf.User.FILTER_FALSE_PROBABILITY);
    }

    public Integer getUserCacheTimeoutMin() {
        return Integer.parseInt(zkUtil.getForPath(ZookeeperNodePaths.CacheConf.User.TIMEOUT_MIN));
    }

    public Integer getUserCacheTimeoutMax() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.TIMEOUT_MAX);
    }

    public Integer getUIDMapCacheTimeoutMin() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.UID_MAP_TIMEOUT_MAX);
    }

    public Integer getUIDMapCacheTimeoutMax() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.UID_MAP_TIMEOUT_MIN);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000L, multiplier = 2))
    @Override
    public void setUser(User user) {
        redis.set(USER.key(user.getId()), toJsonPrettyStr(user), RandomUtil.randomInt(1, 5), TimeUnit.MINUTES);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000L, multiplier = 2))
    @Override
    public void setUserAndOpenIDMap(UserBind userBind) {
        User user = userBind.getUser();
        String userCacheKey = USER.key(user.getId());
        String openIDMapKey = USER_OPEN_ID_AND_ID_MAP.key(userBind.getOpenId());
        redis.multiSet(new HashMap<String, String>() {{
            put(userCacheKey, toJsonPrettyStr(user));
            put(openIDMapKey, user.getId().toString());
        }});
        redis.expire(userCacheKey, RandomUtil.randomInt(getUserCacheTimeoutMin(), getUserCacheTimeoutMax()), MINUTES);
        redis.expire(openIDMapKey, RandomUtil.randomInt(getUIDMapCacheTimeoutMin(), getUIDMapCacheTimeoutMax()), MINUTES);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 5000L, multiplier = 2))
    @Override
    public void setUserAndPhoneMap(User user) {
        String userCacheKey = USER.key(user.getId());
        String phoneMapKey = USER_PHONE_AND_ID_MAP.key(user.getPhone());
        redis.multiSet(new HashMap<String, String>() {{
            put(userCacheKey, toJsonPrettyStr(user));
            put(phoneMapKey, user.getId().toString());
        }});
        redis.expire(userCacheKey, RandomUtil.randomInt(getUserCacheTimeoutMin(), getUserCacheTimeoutMax()), MINUTES);
        redis.expire(phoneMapKey, RandomUtil.randomInt(getUIDMapCacheTimeoutMin(), getUIDMapCacheTimeoutMax()), MINUTES);
    }

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


        if(uidMapIsExist(uid)) {
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

    @Override
    public VXUser searchByOpenID(String openid) throws IllegalArgumentException {
        return redissonUtil.lockExec(() -> {
            Long uid = searchUid(openid);
            if(nonNull(uid)) {
                String userCacheKey = USER.key(uid);
                User user = redis.get(userCacheKey, User.class);
                if(isNull(user)) {
                    user = searchByUID(uid);
                    redis.set(userCacheKey, user, RandomUtil.randomInt(getUserCacheTimeoutMin(), getUserCacheTimeoutMax()), MINUTES);
                }
                return new VXUser(user, openid);
            } else {
                checkArgument(bloomFilter().contains(openid));
                UserBind userBind = dao.searchUserBind(openid);
                if(nonNull(userBind)) {
                    setUserAndOpenIDMap(userBind);
                }
                return new VXUser(userBind.getUser(), openid);
            }
        },
                redisson.getSpinLock(USER.LOCK.key(openid)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }

    private RBloomFilter<Object> bloomFilter() {
        RBloomFilter<Object> filter = redisson.getBloomFilter(getFilterKey());
        filter.tryInit(getExpectedInsertions(), getFalseProbability());
        return filter;
    }

    private Boolean uidMapIsExist(String str) {
        return isNotBlank(str);
    }

    private String searchUidFromRedis(String email) {
        return redis.getOpt(USER_EMAIL_AND_ID_MAP.key(email)).get();
    }

    private Long searchUid(String openid) {
        String uidStr = redis.getOpt(USER_OPEN_ID_AND_ID_MAP.key(openid)).get();
        return uidMapIsExist(uidStr) ? Long.valueOf(uidStr) : null;
    }

    private Long searchUid(Integer phone) {
        String uidStr = redis.getOpt(USER_PHONE_AND_ID_MAP.key(phone)).get();
        return uidMapIsExist(uidStr) ? Long.valueOf(uidStr) : null;
    }

    private void setUserAndEmailMapToRedis(User user) {
        redis.multiSet(new HashMap<String, String>() {{
            put(USER_EMAIL_AND_ID_MAP.key(user.getEmail()), String.valueOf(user.getId()));
            put(USER.key(user.getId()), toJsonPrettyStr(user));
        }});
    }

    private User searchUserFromDBElseThrow(String email) {
        return service.searchByEmailElseThrow(email);
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

    @Override
    public User searchByPhone(Integer phone) throws IllegalArgumentException {
        return redissonUtil.lockExec(() -> {
                    Long uid = searchUid(phone);
                    if(nonNull(uid)) {
                        String userCacheKey = USER.key(uid);
                        User user = redis.get(userCacheKey, User.class);
                        if(isNull(user)) {
                            user = searchByUID(uid);
                            redis.set(userCacheKey, user, RandomUtil.randomInt(getUserCacheTimeoutMin(), getUserCacheTimeoutMax()), MINUTES);
                        }
                        return user;
                    } else {
                        User user = dao.selectByPhone(phone);
                        checkArgument(nonNull(user), "手机号未注册，请检查输入是否正确");
                        setUserAndPhoneMap(user);
                        return user;
                    }
                },
                redisson.getSpinLock(USER_PHONE_AND_ID_MAP.LOCK.key(phone)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }

    @Override
    public User searchByPhone(String phone) throws IllegalArgumentException {
        checkArgument(isNoneBlank(phone) && phone.length() == 11);
        return searchByPhone(Integer.valueOf(phone));
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
        User user = searchByUID(uid);
        redis.set(USER.key(uid), user, timeout, unit);
        return user;
    }

    private User searchByUID(Long uid) throws IllegalArgumentException {
        return service.getOptById(uid).orElseThrow(() -> new IllegalArgumentException("对应 ID 用户不存在！"));
    }

    private void load(User user, int timeout, TimeUnit unit) {
        redis.set(USER.key(user.getId()), user, timeout, unit);
    }

    @Autowired
    public UserCacheImpl(UserService service, UserDao dao) {
        this.service = service;
        this.dao = dao;
    }
}
