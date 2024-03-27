package com.bbs.appService;

import com.bbs.service.GroupService;
import com.bbs.service.RoleGroupService;
import com.bbs.service.RoleService;
import com.bbs.service.UserGroupService;
import com.bbs.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AppManagementService {

    private GroupService groupService;

    private RoleService roleService;

    private UserService userService;

    private UserGroupService userGroupService;

    private RoleGroupService roleGroupService;















    @Resource
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    @Resource
    public void setRuleService(RoleService roleService) {
        this.roleService = roleService;
    }
    @Resource
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Resource
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }
    @Resource
    public void setRoleGroupService(RoleGroupService roleGroupService) {
        this.roleGroupService = roleGroupService;
    }
}
