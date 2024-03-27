package com.bbs.cache.impl;

import cn.hutool.core.util.RandomUtil;
import com.bbs.cache.UserCache;
import com.bbs.dao.UserDao;
import com.bbs.entity.User;
import com.bbs.entity.UserBind;
import com.bbs.entity.VXUser;
import com.bbs.enums.ZookeeperNodePaths;
import com.bbs.service.UserBindService;
import com.bbs.service.UserService;
import com.bbs.util.RedisUtil;
import com.bbs.util.ZKUtil;
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
import static com.bbs.enums.RedisKeys.*;
import static com.bbs.util.RedisUtil.Redisson.*;
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

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserDao dao;

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
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.UID_MAP_TIMEOUT_MIN);
    }

    public Integer getUIDMapCacheTimeoutMax() {
        return zkUtil.getIntForPath(ZookeeperNodePaths.CacheConf.User.UID_MAP_TIMEOUT_MAX);
    }

    @Retryable(value = RestClientException.class, backoff = @Backoff(delay = 5000L, multiplier = 2))
    @Override
    public void setUser(User user) {
        redis.set(USER.key(user.getId()), toJsonPrettyStr(user), RandomUtil.randomInt(1, 5), TimeUnit.MINUTES);
    }

    @Retryable(value = RestClientException.class, backoff = @Backoff(delay = 5000L, multiplier = 2))
    @Override
    public void setUserAndOpenIDMap(UserBind userBind) {
        User user = userBind.getUser();
        String userCacheKey = getUserCacheKey(user);
        String openIDMapKey = getUserIDAndOpenIDMap(userBind);
        redis.multiSet(new HashMap<String, String>() {{
            put(userCacheKey, toJsonPrettyStr(user));
            put(openIDMapKey, user.getId().toString());
        }});
        setUserCacheExpire(userCacheKey);
        setUserIDAndOpenIDMapExpire(openIDMapKey);
    }

    @Retryable(value = RestClientException.class, backoff = @Backoff(delay = 5000L, multiplier = 2))
    @Override
    public void setUserAndPhoneMap(User user) {
        String userCacheKey = getUserCacheKey(user);
        String phoneMapKey = getUserIDAndPhoneMapKey(user);
        redis.multiSet(new HashMap<String, String>() {{
            put(userCacheKey, toJsonPrettyStr(user));
            put(phoneMapKey, user.getId().toString());
        }});
        setUserCacheExpire(userCacheKey);
        setUserIDAndPhoneMapExpire(phoneMapKey);
    }

    @Override
    public void setUserAndPhoneAndOpenIDMap(User user, String openID) {
        String userCacheKey = getUserCacheKey(user);
        String openIDMapKey = getUserIDAndOpenIDMap(openID);
        String phoneMapKey = getUserIDAndPhoneMapKey(user);
        redis.multiSet(new HashMap<String, String>() {{
            put(openIDMapKey, user.getId().toString());
            put(phoneMapKey, user.getId().toString());
        }});
        setUserCacheExpire(userCacheKey);
        setUserIDAndOpenIDMapExpire(openIDMapKey);
        setUserIDAndPhoneMapExpire(phoneMapKey);
    }

    private String getUserCacheKey(User user) {
        return USER.key(user.getId());
    }

    private String getUserIDAndOpenIDMap(UserBind userBind) {
        return getUserIDAndOpenIDMap(userBind.getOpenId());
    }

    private String getUserIDAndOpenIDMap(String openID) {
        return USER_OPEN_ID_AND_ID_MAP.key(openID);
    }

    private String getUserIDAndPhoneMapKey(User user) {
        return getUserIDAndPhoneMapKey(user.getPhone());
    }

    @Override
    public String getUserIDAndPhoneMapKey(String phone) {
        return getUserIDAndPhoneMapKey(Long.valueOf(phone));
    }

    private String getUserIDAndPhoneMapKey(Long phone) {
        return USER_PHONE_AND_ID_MAP.key(phone);
    }

    @Override
    public void setUserCacheExpire(String userCacheKey) {
        redis.expire(userCacheKey, RandomUtil.randomInt(getUserCacheTimeoutMin(), getUserCacheTimeoutMax()), MINUTES);
    }

    public void setUserIDAndOpenIDMapExpire(String openIDMapKey) {
        redis.expire(openIDMapKey, RandomUtil.randomInt(getUIDMapCacheTimeoutMin(), getUIDMapCacheTimeoutMax()), MINUTES);
    }

    @Override
    public void setUserIDAndPhoneMapExpire(String phoneMapKey) {
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
            if(nonNull(uid)) {  // 微信已经绑定账号了
                String key = USER.key(uid);
                User user = redis.get(key, User.class);
                if(isNull(user)) {  // 长时间未登录，需要重新加载数据到缓存
                    user = searchDBByUID(uid);
                    redis.set(key, user, RandomUtil.randomInt(getUserCacheTimeoutMin(), getUserCacheTimeoutMax()), MINUTES);
                }
                return new VXUser(user, openid);
            } else {
                UserBind userBind = userBindService.searchUserBind(openid);
                checkArgument(nonNull(userBind), "微信未绑定账号，请绑定账号后重试");
                //微信已经绑定账号了，但长时间未登录，导致缓存数据清除
                setUserAndOpenIDMap(userBind);
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

    private Long searchUid(Long phone) {
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
    public User searchOrRegisterByPhone(Long phone) throws IllegalArgumentException {
        return redissonUtil.lockExec(() -> {
                    Long uid = searchUid(phone);
                    User user;
                    if(nonNull(uid)) {
                        String userCacheKey = USER.key(uid);
                        user = redis.get(userCacheKey, User.class);
                        if(isNull(user)) {  //长时间未登录，缓存的用户信息被删除，需要重新设置
                            user = searchDBByUID(uid);
                            redis.set(userCacheKey, user, RandomUtil.randomInt(getUserCacheTimeoutMin(), getUserCacheTimeoutMax()), MINUTES);
                        }
                        return user;
                    } else {
                        // 缓存无法查询，按 phone 从数据库查询，加载并重置缓存中的映射和用户信息
                        user = dao.selectByPhone(phone);
                        if(nonNull(user)) {
                            setUserAndPhoneMap(user);
                        } else {
                            // 注册
                            user = service.registerByPhoneNoLockAndNoLoadCache(phone);
                            setUserAndPhoneMap(user);
                        }
                    }
                    return user;
                },
                redisson.getSpinLock(USER_PHONE_AND_ID_MAP.LOCK.key(phone)),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.WAIT),
                zkUtil.getIntForPath(ZookeeperNodePaths.LockConf.UserCache.LEASE),
                MILLISECONDS
        );
    }

    @Override
    public User searchOrRegisterByPhone(String phone) throws IllegalArgumentException {
        checkArgument(isNoneBlank(phone) && phone.length() == 11);
        return searchOrRegisterByPhone(Long.valueOf(phone));
    }

    /**
     * 通过手机号查询用户（需要加锁！！！！）
     * @param phone 手机号
     * @return 用户/NULL
     */
    @Override
    public User searchByPhoneNoLockNoLoad(String phone) throws InterruptedException {
        try {
            Long uid = searchUIDByCacheThrow(phone);
            User user;
            if (nonNull(uid)) {
                user = search(uid);
            } else {
                user = dao.selectByPhone(phone);
            }
            return user;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public Long searchUIDByCache(String phone) {
        String phoneMapKey = getUserIDAndPhoneMapKey(phone);
        String uidStr = redis.get(phoneMapKey);
        return isNotBlank(uidStr) ? Long.valueOf(uidStr) : null;
    }

    @Override
    public Long searchUIDByCacheThrow(String phone) throws IllegalArgumentException {
        Long uid = searchUIDByCache(phone);
        checkArgument(nonNull(uid), "UID 对应用户不存在");
        return uid;
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
        User user = searchDBByUID(uid);
        redis.set(USER.key(uid), user, timeout, unit);
        return user;
    }

    private User searchDBByUID(Long uid) throws IllegalArgumentException {
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
