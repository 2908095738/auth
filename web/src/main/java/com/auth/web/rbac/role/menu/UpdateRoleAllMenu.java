package com.auth.web.rbac.role.menu;

import com.bbs.Result;
import com.bbs.auth.entity.RoleMenu;
import com.bbs.auth.entity.rbac.Role;
import com.bbs.auth.service.RoleMenuService;
import com.bbs.auth.service.RoleService;
import com.bbs.auth.service.SystemRouterService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * 更新角色的全部权限
 * @author ext.luchenlin5
 */
@RestController
public class UpdateRoleAllMenu {

    @Resource
    private RoleService roleService;

    @Resource
    private SystemRouterService systemRouterService;

    @Resource
    private RoleMenuService roleMenuService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long roleId;

        private List<Long> menuIds;
    }

    @PostMapping("/role/menu")
    @Transactional
    public Result<Boolean> edit(@RequestBody Param param) {
        Role role = roleService.search(param.getRoleId());
        // 如果传输的是部分菜单id，则先删除该角色下所有的菜单，再新增
        // 如果传输的时空集，则删除该角色所有的菜单
        roleMenuService.lambdaUpdate().eq(RoleMenu::getRoleId, role.getId()).remove();
        if(isNotEmpty(param.getMenuIds())) {
            roleMenuService.saveBatch(param.getMenuIds().stream().map(menuId -> new RoleMenu(param.getRoleId(), menuId)).collect(Collectors.toList()));
        }
        return Result.success();
    }
}
