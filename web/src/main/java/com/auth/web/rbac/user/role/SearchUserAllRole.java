package com.auth.web.rbac.user.role;

import com.auth.Result;
import com.auth.rbac.impl.dto.RoleDTO;
import com.auth.rbac.user.role.UserRole;
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
    private UserRole userRole;
    @GetMapping("/user/role")
    public Result<VO> search(Param param) {
        List<RoleDTO> allRoleList = userRole.searchListBySystemCodeJoinSystem(param.getSystemCode());
        List<RoleDTO> userRoleList = userRole.searchListBySystemCodeAndUserIdJoinSystem(param.systemCode, param.userId);
        return Result.success(new VO(allRoleList, userRoleList));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private String systemCode;

        private Long userId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {

        private List<RoleDTO> allRoles;

        private List<RoleDTO> userRoles;
    }
}
