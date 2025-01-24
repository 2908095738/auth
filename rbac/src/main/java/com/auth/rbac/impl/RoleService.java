package com.auth.rbac.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface RoleService {

    Result<Role> search(Role param);

    Role search(Long id);

    Page<Role> searchJoinSystemPage(Integer current, Integer size);

    List<Role> searchBySystemCodeJoinSystemList(String systemCode);

    List<Role> searchBySystemCodeAndUidJoinSystemList(String systemCode, Long userId);
}
