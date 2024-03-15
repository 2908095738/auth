package com.auth.appService;

import com.auth.service.GroupService;
import com.auth.service.RoleGroupService;
import com.auth.service.RoleService;
import com.auth.service.UserGroupService;
import com.auth.service.UserService;
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
