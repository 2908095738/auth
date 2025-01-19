package com.bbs.auth.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.auth.entity.rbac.Role;
import com.bbs.Result;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 *
 * @author ext.luchenlin5
 */
public interface RoleService extends MPJBaseService<Role> {

    Result<Role> search(Role param);

    Role search(Long id);

    Page<Role> searchJoinSystemPage(Integer current, Integer size);

    List<Role> searchBySystemCodeJoinSystemList(String systemCode);

    List<Role> searchBySystemCodeAndUidJoinSystemList(String systemCode, Long userId);
}
