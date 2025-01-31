package com.auth.web.login;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpStatus;
import com.auth.Result;
import com.auth.config.Config;
import com.auth.config.impl.entity.RedisLockConfig;
import com.auth.log.Log;
import com.auth.login.AbstractLoginStrategy;
import com.auth.login.LoginStrategy;
import com.auth.login.param.Param;
import com.auth.login.vo.VO;
import com.auth.token.Token;
import com.auth.token.impl.dto.UserLoginToken;
import com.auth.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.Date;

import static com.auth.Result.failed;
import static com.auth.Result.success;

@Slf4j
@RestController
@RequestMapping
public class Login implements InitializingBean {

    @Resource
    private Token.CreateUserLoginAuthToken createToken;

    @Resource
    private RedissonClient redisson;

    @Resource
    private LoginStrategy loginStrategyFactory;

    private static final String CACHE_CODE = "lock_login";

    private static RedisLockConfig lockConfig;

    @Override
    public void afterPropertiesSet() {
        lockConfig = SpringUtil.getBean(Config.LockConfig.class).getConfig(CACHE_CODE);
    }

    @PostMapping("/login")
    public Result<VO> login(@Valid @RequestBody Param param) throws IllegalArgumentException, InterruptedException {
        RLock lock = getLock(param);
        // 尝试加锁
        if(tryLock(lock)) {
            // 开始登录计时
            TimeInterval timer = DateUtil.timer();
            // 登录
            UserDTO user = tryLogin(param);
            // 记录登录日志
            recordLog(user, timer);
            // 解锁
            unlock(lock);
            // 创建 Token
            UserLoginToken tokenInfo = createToken(user);
            return success(new VO(user.getId(), user.getName(), tokenInfo.getToken()));
        } else {
            return failed(HttpStatus.HTTP_BAD_REQUEST, "短时间内请勿重复登录！");
        }
    }

    private RLock getLock(Param param) {
        return redisson.getSpinLock(lockConfig.generateKey(param.getPhone()));
    }

    private Boolean tryLock(RLock lock) throws InterruptedException {
        return lock.tryLock(lockConfig.getWaitTime(), lockConfig.getLeaseTime(), lockConfig.getUnit());
    }

    private UserDTO tryLogin(Param param) {
        AbstractLoginStrategy loginStrategy = loginStrategyFactory.getInstance(param.getLoginType());
        return loginStrategy.tryLogin(param);
    }

    private UserLoginToken createToken(UserDTO user) {
        return createToken.create(user.getId());
    }

    private void recordLog(UserDTO user, TimeInterval timer) {
        Log.Login.record(user.getId(), new Date(), timer.interval());
    }

    private void unlock(RLock lock) {
        if(lock.isLocked()) {
            lock.unlock();
        }
    }
}
