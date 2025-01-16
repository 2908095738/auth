package com.bbs.auth.service;


import com.bbs.auth.controller.RoleController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.auth.entity.Role;
import com.bbs.Result;
import com.github.yulichang.base.MPJBaseService;

/**
 *
 */
public interface RoleService extends MPJBaseService<Role> {

    Result<Role> search(Role param);

    Page<Role> searchJoinSystem(Integer current, Integer size);
}
