package com.bbs.auth.app.rbac.user;

import com.bbs.Result;
import com.bbs.auth.entity.rbac.UserRole;
import com.bbs.auth.service.UserRoleService;
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

/**
 * 更新用户的全部角色
 * @author ext.luchenlin5
 */
@RestController
public class UpdateUserAllRole {


    @Resource
    private UserRoleService userRoleService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private Long userId;

        private String systemCode;

        private List<Long> roleIds;
    }

    @Transactional
    @PostMapping("/user/role")
    public Result<Boolean> update(@RequestBody Param param) {
        userRoleService.lambdaUpdate().eq(UserRole::getSystemCode, param.getSystemCode()).eq(UserRole::getUserId, param.getUserId()).remove();
        userRoleService.saveBatch(param.getRoleIds().stream().map(roleId -> new UserRole(param.getUserId(), param.getSystemCode(), roleId)).collect(Collectors.toList()));
        return Result.success();
    }
}
