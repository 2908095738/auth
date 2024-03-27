package com.bbs.service;

import com.bbs.controller.RoleController;
import com.bbs.entity.Resource;
import com.bbs.entity.RoleResource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;

/**
* @author 路晨霖
* @description 针对表【role_resource(角色 & 资源关联表)】的数据库操作Service
* @createDate 2023-12-28 19:51:38
*/
public interface RoleResourceService extends IService<RoleResource> {

    Result<Page<Resource>> search(RoleController.SearchResourcePermissionParam param);

    Result<Boolean> update(RoleController.AddResourcePermissionParam param);
}
