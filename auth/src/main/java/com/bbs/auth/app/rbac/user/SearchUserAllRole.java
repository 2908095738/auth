package com.bbs.auth.app.rbac.user;

import com.bbs.Result;
import com.bbs.auth.entity.rbac.Role;
import com.bbs.auth.service.RoleService;
import com.bbs.auth.service.UserRoleService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询用户的全部角色
 * @author ext.luchenlin5
 */
@RestController
public class SearchUserAllRole {

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RoleService roleService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private String systemCode;

        private Long userId;
    }

    @GetMapping("/user/role")
    public Result<VO> search(Param param) {
        List<Role> allRoleList = roleService.searchBySystemCodeJoinSystemList(param.getSystemCode());
        List<Role> userRoleList = roleService.searchBySystemCodeAndUidJoinSystemList(param.systemCode, param.userId);
        return Result.success(new VO(allRoleList, userRoleList));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {

        private List<Role> allRoles;

        private List<Role> userRoles;
    }
}
