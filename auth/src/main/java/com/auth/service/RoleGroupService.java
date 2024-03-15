package com.auth.service;

import com.auth.entity.RoleGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.auth.dto.RoleGroupVo;

import java.util.List;

/**
 *
 */
public interface RoleGroupService extends IService<RoleGroup> {
    List<RoleGroupVo> selectRoleAndGroupNameList();
}
