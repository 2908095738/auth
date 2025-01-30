package com.auth.web.rbac.system;

import cn.hutool.core.bean.BeanUtil;
import com.auth.Result;
import com.auth.rbac.dto.SystemDTO;
import com.auth.rbac.user.role.RBAC;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@RestController
@RequestMapping
public class CreateSystem {

    @Resource
    private RBAC.System rbacSystem;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Param implements Serializable {

        @NotBlank
        private String name;

        private String description;

        @NotNull
        private Long admin;

        private Long pid;
    }

    @PutMapping("/system")
    public Result<Boolean> create(@Valid @RequestBody Param param) {
        SystemDTO systemDTO = new SystemDTO();
        BeanUtil.copyProperties(param, new SystemDTO());
        return Result.success(rbacSystem.save(systemDTO));
    }
}
