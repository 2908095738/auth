package com.bbs.auth.service;

import com.bbs.auth.app.config.bloom.config.CheckPhoneIsRegisterConfig;
import com.bbs.auth.entity.BloomFilterInitLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 路晨霖
* @description 针对表【bloom_filter_init_log(布隆过滤器初始化执行日志)】的数据库操作Service
* @createDate 2024-06-27 11:02:30
*/
public interface BloomFilterInitLogService extends IService<BloomFilterInitLog> {

    BloomFilterInitLog generateLog(String businessCode, String key, String describe, CheckPhoneIsRegisterConfig config);
}
