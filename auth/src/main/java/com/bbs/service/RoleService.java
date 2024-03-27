package com.bbs.service;


import com.bbs.controller.RoleController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.entity.Role;
import com.clinic.Result;

/**
 *
 */
public interface RoleService extends IService<Role> {

    Result<Page<Role>> search(RoleController.QueryRoleParam param);
}
