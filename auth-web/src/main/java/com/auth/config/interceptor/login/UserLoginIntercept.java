package com.auth.config.interceptor.login;

import com.auth.token.Token;
import com.auth.token.impl.dto.UserLoginToken;
import com.auth.user.User;
import com.auth.user.cache.UserCache;
import com.auth.user.impl.config.threadlocal.LoginUserThreadLocal;
import com.auth.user.dto.UserDTO;
import com.auth.user.exception.UserNotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isNull;

/**
 * 登录拦截器
 * @author ext.luchenlin5
 */
@Slf4j
@Component
public class UserLoginIntercept implements HandlerInterceptor {

    @Resource
    private Token.ParseUserLoginToken parseUserLoginToken;

    @Resource
    private Token.VerifyUserLoginAuthToken verifyUserLoginAuthToken;

    @Resource
    private User.Search searchUser;

    @Resource
    private UserCache userCache;

    @Resource
    private RedissonClient redisson;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws UserNotLoginException {
        if(IgnoreConfig.match(request.getRequestURI())){
            return true;
        }
        try {
            if(verifyUserLoginAuthToken.verify(request)) {
                UserLoginToken token = parseUserLoginToken.parse(request);
                Long uid = token.getUid();

                RBloomFilter<Long> bloomFilter = redisson.getBloomFilter("user-id-bloom-filter");
                bloomFilter.tryInit(1000000, 0.01);
                if(bloomFilter.contains(uid)) {
                    UserDTO user = userCache.get(uid);
                    if(isNull(user)) {
                        user = searchUser.byId(uid);
                        userCache.reload(user);
                    }
                    LoginUserThreadLocal.set(user);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("[UserServiceImpl::loginUser] 用户登录信息获取异常 ", e);
            throw new UserNotLoginException();
        }
    }


    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) {
        // 接口访问结束后，从 ThreadLocal 中删除用户信息
        LoginUserThreadLocal.remove();
    }
}
