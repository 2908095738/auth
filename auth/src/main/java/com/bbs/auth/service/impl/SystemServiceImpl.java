package com.bbs.auth.service.impl;

import com.bbs.auth.entity.System;
import com.bbs.auth.service.SystemService;
import com.bbs.auth.mapper.SystemMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author 路晨霖
* @description 针对表【system】的数据库操作Service实现
* @createDate 2024-04-14 13:45:45
*/
@Service
public class SystemServiceImpl extends MPJBaseServiceImpl<SystemMapper, System>
    implements SystemService{

}




