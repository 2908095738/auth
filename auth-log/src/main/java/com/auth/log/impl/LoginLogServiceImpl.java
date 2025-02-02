package com.auth.log.impl;

import com.auth.log.entity.LoginLog;
import com.auth.log.mapper.LoginLogMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
public class LoginLogServiceImpl extends MPJBaseServiceImpl<LoginLogMapper, LoginLog> {
}
