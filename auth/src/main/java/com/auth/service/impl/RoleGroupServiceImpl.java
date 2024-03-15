package com.auth.service.impl;

import com.auth.dto.RoleGroupVo;
import com.auth.entity.RoleGroup;
import com.auth.mapper.RoleGroupMapper;
import com.auth.service.RoleGroupService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class RoleGroupServiceImpl extends MPJBaseServiceImpl<RoleGroupMapper, RoleGroup>
    implements RoleGroupService{

    @Override
    public List<RoleGroupVo> selectRoleAndGroupNameList() {
        return baseMapper.selectRoleGroupNameList();
    }
}




