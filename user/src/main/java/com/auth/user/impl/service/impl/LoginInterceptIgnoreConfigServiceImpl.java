package com.auth.user.impl.service.impl;

import com.auth.user.entity.LoginInterceptIgnoreConfig;
import com.auth.user.impl.mapper.LoginInterceptIgnoreConfigMapper;
import com.auth.user.impl.service.LoginInterceptIgnoreConfigService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录拦截忽略配置
 * @author ext.luchenlin5
 */
@Slf4j
@Service
public class LoginInterceptIgnoreConfigServiceImpl extends MPJBaseServiceImpl<LoginInterceptIgnoreConfigMapper, LoginInterceptIgnoreConfig> implements LoginInterceptIgnoreConfigService {
}
