package com.bbs.service.impl;

import com.bbs.dto.RoleGroupVo;
import com.bbs.entity.RoleGroup;
import com.bbs.mapper.RoleGroupMapper;
import com.bbs.service.RoleGroupService;
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




