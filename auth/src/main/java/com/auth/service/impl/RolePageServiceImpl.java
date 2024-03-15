package com.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth.entity.RolePage;
import com.auth.mapper.RolePageMapper;
import com.auth.service.RolePageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RolePageServiceImpl extends ServiceImpl<RolePageMapper, RolePage> implements RolePageService {
}
