package com.auth.web.rbac.menu;

import com.auth.Result;
import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.user.role.RBAC;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController("updateSystemRouter")
@RequestMapping
public class Update {

    @Resource
    private RBAC.Menu rbacMenu;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Param {

        private List<Long> ids;

        private Integer type;
    }

    @PostMapping("/menu/batch")
    public Result<Boolean> batchUpdate(@RequestBody Param param) {
        return Result.success(rbacMenu.updateTypeById(param.getIds(), param.getType()));
    }

    @PostMapping("/menu")
    public Result<Boolean> update(@RequestBody MenuDTO menuDTO) {
        return Result.success(rbacMenu.updateById(menuDTO));
    }
}
