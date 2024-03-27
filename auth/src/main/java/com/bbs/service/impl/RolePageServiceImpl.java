package com.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.entity.RolePage;
import com.bbs.mapper.RolePageMapper;
import com.bbs.service.RolePageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RolePageServiceImpl extends ServiceImpl<RolePageMapper, RolePage> implements RolePageService {
}
