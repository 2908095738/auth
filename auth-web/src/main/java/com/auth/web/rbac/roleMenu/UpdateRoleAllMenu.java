package com.auth.web.rbac.roleMenu;

import com.auth.Result;
import com.auth.rbac.dto.RoleDTO;
import com.auth.rbac.user.role.RBAC;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * 更新角色的全部权限
 * @author ext.luchenlin5
 */
@RestController
public class UpdateRoleAllMenu {

    @Resource
    private RBAC.Role rbacRole;

    @Resource
    private RBAC.RoleMenu rbacRoleMenu;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long roleId;

        private List<Long> menuIds;
    }

    @Transactional
    @PostMapping("/role/menu")
    public Result<Boolean> edit(@RequestBody Param param) {
        RoleDTO role = rbacRole.searchById(param.getRoleId());
        // 如果传输的是部分菜单id，则先删除该角色下所有的菜单，再新增
        // 如果传输的时空集，则删除该角色所有的菜单
        Boolean delResult = rbacRoleMenu.removeByRoleId(role.getId());
        if(delResult && isNotEmpty(param.getMenuIds())) {
            return Result.success(rbacRoleMenu.save(param.getRoleId(), param.menuIds));
        }
        return Result.of(delResult);
    }
}
