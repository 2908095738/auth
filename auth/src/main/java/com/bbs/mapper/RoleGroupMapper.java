package com.bbs.mapper;

import com.bbs.dto.RoleGroupVo;
import com.bbs.entity.RoleGroup;

import com.github.yulichang.base.MPJBaseMapper;

import java.util.List;

/**
 * @Entity com.clinic.entity.RoleGroup
 */
public interface RoleGroupMapper extends MPJBaseMapper<RoleGroup> {
    List<RoleGroupVo> selectRoleGroupNameList();
}




