package com.bbs.service;

import com.bbs.entity.RoleGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.dto.RoleGroupVo;

import java.util.List;

/**
 *
 */
public interface RoleGroupService extends IService<RoleGroup> {
    List<RoleGroupVo> selectRoleAndGroupNameList();
}
