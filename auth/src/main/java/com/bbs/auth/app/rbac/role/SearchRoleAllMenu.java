package com.bbs.auth.app.rbac.role;

import cn.hutool.core.lang.tree.Tree;
import com.bbs.Result;
import com.bbs.auth.entity.rbac.Role;
import com.bbs.auth.service.RoleMenuService;
import com.bbs.auth.service.RoleService;
import com.bbs.auth.service.SystemRouterService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询角色的全部菜单
 * @author lcl
 */
@RestController
public class SearchRoleAllMenu {

    @Resource
    private RoleService roleService;

    @Resource
    private SystemRouterService systemRouterService;

    @Resource
    private RoleMenuService roleMenuService;

    @GetMapping("/role/menu")
    public Result<VO> search(@RequestParam Long roleId) {
        Role role = roleService.search(roleId);
        return Result.success(systemRouterService.searchTreeBySystemAndRoleId(role.getSystem().getId(), roleId));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VO {

        private List<Tree<Long>> tree;

        private List<Long> roleMenuIds;
    }
}
