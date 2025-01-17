package com.bbs.auth.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.auth.entity.Role;
import com.bbs.Result;
import com.github.yulichang.base.MPJBaseService;

/**
 *
 * @author ext.luchenlin5
 */
public interface RoleService extends MPJBaseService<Role> {

    Result<Role> search(Role param);

    Role search(Long id);

    Page<Role> searchJoinSystem(Integer current, Integer size);
}
