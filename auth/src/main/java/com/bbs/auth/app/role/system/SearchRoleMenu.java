package com.bbs.auth.app.role.system;

import com.bbs.Result;
import com.bbs.auth.entity.Role;
import com.bbs.auth.entity.RoleMenu;
import com.bbs.auth.service.RoleMenuService;
import com.bbs.auth.service.RoleService;
import com.bbs.auth.service.SystemRouterService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * @author lcl
 */
@RestController
public class SearchRoleMenu {

    @Resource
    private RoleService roleService;

    @Resource
    private SystemRouterService systemRouterService;

    @Resource
    private RoleMenuService roleMenuService;

    @GetMapping("/role/menu")
    public Result<SearchRoleMenuVO> search(@RequestParam Long roleId) {
        Role role = roleService.search(roleId);
        return Result.success(systemRouterService.searchTreeBySystemAndRoleId(role.getSystem().getId(), roleId));
    }

    @PostMapping("/role/menu")
    @Transactional
    public Result<Boolean> edit(@RequestBody EditRoleMenuParam param) {
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
