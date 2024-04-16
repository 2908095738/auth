package com.bbs.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.log.entity.LogLogin;
import com.bbs.log.service.LogLoginService;
import com.bbs.log.mapper.LogLoginMapper;
import org.springframework.stereotype.Service;

@Service
public class LogLoginServiceImpl extends ServiceImpl<LogLoginMapper, LogLogin>
    implements LogLoginService{

}




