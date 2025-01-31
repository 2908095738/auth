package com.auth.web.rbac.roleMenu;

import com.auth.Result;
import com.auth.rbac.dto.RoleDTO;
import com.auth.rbac.user.role.RBAC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 查询角色的全部菜单
 * @author lcl
 */
@RestController
public class SearchRoleAllMenu {

    @Resource
    private RBAC.Role rbacRole;

    @Resource
    private RBAC.Menu rbacMenu;

    @GetMapping("/role/menu")
    public Result<RBAC.Menu.SearchTreeBySystemAndRoleIdVO> search(@RequestParam Long roleId) {
        RoleDTO role = rbacRole.searchById(roleId);
        return Result.success(rbacMenu.searchTree(role.getSystemDTO().getId(), roleId));
    }
}
