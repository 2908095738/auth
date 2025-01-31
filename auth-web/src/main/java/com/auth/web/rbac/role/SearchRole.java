package com.auth.web.rbac.role;

import com.auth.Result;
import com.auth.rbac.dto.RoleDTO;
import com.auth.rbac.user.role.RBAC;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("SearchRoleController")
public class SearchRole {

    @Resource
    private RBAC.Role rbacRole;


    @GetMapping("/role/page")
    public Result<Page<RoleDTO>> queryRole(@RequestParam(required = false) Integer current, @RequestParam(required = false) Integer size){
        return Result.success(rbacRole.page(current, size));
    }
}
