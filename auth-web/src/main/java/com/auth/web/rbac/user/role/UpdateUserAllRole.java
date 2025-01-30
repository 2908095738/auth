package com.auth.web.rbac.user.role;

import com.auth.Result;
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

/**
 * 更新用户的全部角色
 * @author ext.luchenlin5
 */
@RestController
public class UpdateUserAllRole {

    @Resource
    private RBAC.UseRole useRole;

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
        useRole.add(param.getSystemCode(), param.getUserId(), param.getRoleIds());
        return Result.success();
    }
}
