package com.auth.mapper;

import com.auth.dto.RoleGroupVo;
import com.auth.entity.RoleGroup;

import com.github.yulichang.base.MPJBaseMapper;

import java.util.List;

/**
 * @Entity com.clinic.entity.RoleGroup
 */
public interface RoleGroupMapper extends MPJBaseMapper<RoleGroup> {
    List<RoleGroupVo> selectRoleGroupNameList();
}




