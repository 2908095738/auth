package com.auth.web.rbac.menu;

import com.auth.Result;
import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.user.role.RBAC;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("addSystemRouter")
@RequestMapping
public class Add {

    @Resource
    private RBAC.Menu rbacMenu;

    @PutMapping("/menu")
    public Result<Boolean> search(@RequestBody MenuDTO menu) {
        return Result.success(rbacMenu.add(menu));
    }
}
