package com.bbs.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.auth.entity.RoleMenu;
import com.bbs.auth.mapper.RoleMenuMapper;
import com.bbs.auth.service.RoleMenuService;
import org.springframework.stereotype.Service;

/**
 * @author ext.luchenlin5
 */
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {
}
