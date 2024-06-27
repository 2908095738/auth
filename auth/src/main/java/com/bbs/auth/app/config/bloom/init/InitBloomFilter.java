package com.bbs.auth.app.config.bloom.init;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.auth.app.config.bloom.config.CheckPhoneIsRegisterConfig;
import com.bbs.auth.entity.User;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.RedisUtil;
import com.bbs.auth.entity.BloomFilterInitLog;
import com.bbs.auth.service.BloomFilterInitLogService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class InitBloomFilter {

    @Resource
    private BloomFilterInitLogService initLogService;

    @Resource
    private RedissonClient redisson;
    @Resource
    private UserService userService;
    @Resource
    private RedisUtil redisUtil;

    @PostMapping("/config/init/bloom/register/check/phone")
    public Result<Boolean> checkPhoneIsRegister() {

        userService.loginUserIsAdmin(); //校验管理员身份

        CheckPhoneIsRegisterConfig config = SpringUtil.getBean(CheckPhoneIsRegisterConfig.class);
        long expected = config.getExpected();
        double threshold = config.getThreshold();
        double probability = config.getProbability();

        BloomFilterInitLog log = initLogService.generateLog("phone", "bloom:register:phone", "校验用户手机号是否已注册", config);

        log.startCounting();    // 开始计时
        try {
            List<Long> phones = userService.listObjs(new LambdaQueryWrapper<User>().select(User::getPhone));
            long userNumber = phones.size();
            // 如果实际用户数量，小于【init 预计大小】的 75%，则使用该大小执行初始化
            // 例：预计 10w，用户数量大于 7w5，则不执行此次 init
            if(userNumber < expected * threshold) {
                redisUtil.delete(config.getName());
                RBloomFilter<Long> filter = redisson.getBloomFilter(config.getName());
                filter.tryInit(expected, probability);
                filter.add(phones);
                log.success();
            } else {
                log.fail("用户数量大于【init 预计大小 75%】需要调整参数配置！");
            }
        } catch (Exception e) {
            log.fail(e.getMessage());
        }
        initLogService.save(log);
        return Result.success();
    }
}
