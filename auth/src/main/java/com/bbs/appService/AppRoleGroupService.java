package com.bbs.appService;

import com.bbs.dto.RoleGroupVo;
import com.bbs.entity.RoleGroup;
import com.bbs.mapper.RoleGroupMapper;
import com.bbs.service.RoleGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AppRoleGroupService extends ServiceImpl<RoleGroupMapper, RoleGroup> {

    private RoleGroupService roleGroupService;


    public List<RoleGroupVo> selectRoleAndGroupNameList() {
       return roleGroupService.selectRoleAndGroupNameList();
    }

    public boolean saveOrUpdate(RoleGroup roleGroup) {
        return roleGroupService.saveOrUpdate(roleGroup);
    }

    public boolean removeById(Long id) {
        return roleGroupService.removeById(id);
    }








    @Resource
    public void setRoleGroupService(RoleGroupService roleGroupService) {
        this.roleGroupService = roleGroupService;
    }
}
