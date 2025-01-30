package com.auth.web.rbac.system;

import com.auth.Result;
import com.auth.rbac.user.role.RBAC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 修改系统超级管理员
 */
@RestController
@RequestMapping
public class UpdateAdmin {

    @Resource
    private RBAC.System rbacSystem;

    @PostMapping("/system/admin")
    public Result<Boolean> update(
            @RequestParam("id") Long systemId,
            @RequestParam("admin") Long userId
    ) {
        return Result.of(rbacSystem.updateSystemAdmin(systemId, userId));
    }
}
