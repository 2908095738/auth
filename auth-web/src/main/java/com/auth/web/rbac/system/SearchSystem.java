package com.auth.web.rbac.system;

import com.auth.Result;
import com.auth.rbac.dto.SystemDTO;
import com.auth.rbac.user.role.RBAC;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class SearchSystem {

    @Resource
    private RBAC.System rbacSystem;

    @GetMapping("/system/list")
    public Result<List<SystemDTO>> list() {
        return Result.success(rbacSystem.searchSystem());
    }

    @GetMapping("/system/page")
    public Result<Page<SystemDTO>> page(@RequestParam Integer current, @RequestParam Integer size) {
        return Result.success(rbacSystem.searchSystemPage(current, size));
    }
}
