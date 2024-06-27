package com.bbs.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.auth.app.config.bloom.config.CheckPhoneIsRegisterConfig;
import com.bbs.auth.service.UserService;
import com.bbs.auth.entity.BloomFilterInitLog;
import com.bbs.auth.service.BloomFilterInitLogService;
import com.bbs.auth.mapper.BloomFilterInitLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 路晨霖
* @description 针对表【bloom_filter_init_log(布隆过滤器初始化执行日志)】的数据库操作Service实现
* @createDate 2024-06-27 11:02:30
*/
@Service
public class BloomFilterInitLogServiceImpl extends ServiceImpl<BloomFilterInitLogMapper, BloomFilterInitLog>
    implements BloomFilterInitLogService{

    @Resource
    private UserService userService;

    @Override
    public BloomFilterInitLog generateLog(String businessCode, String key, String describe, CheckPhoneIsRegisterConfig config) {
        Long loginUserId = userService.loginUser().getId();
        BloomFilterInitLog log = new BloomFilterInitLog(businessCode, key, describe);
        log.setCreateBy(loginUserId);
        log.initConfig(config);
        return log;
    }
}