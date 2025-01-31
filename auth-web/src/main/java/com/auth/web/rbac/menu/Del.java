package com.auth.web.rbac.menu;

import com.auth.Result;
import com.auth.rbac.user.role.RBAC;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("delSystemRouter")
@RequestMapping
public class Del {

    @Resource
    private RBAC.Menu rbacMenu;

    @DeleteMapping("/menu/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        return Result.of(rbacMenu.del(id));
    }
}
