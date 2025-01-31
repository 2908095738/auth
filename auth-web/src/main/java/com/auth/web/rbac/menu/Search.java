package com.auth.web.rbac.menu;

import cn.hutool.core.lang.tree.Tree;
import com.auth.Result;
import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.user.role.RBAC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Objects.nonNull;

@RestController("searchSystemRouter")
@RequestMapping
public class Search {

    @Resource
    private RBAC.Menu rbacMenu;

    @GetMapping("/menu")
    public Result<MenuDTO> searchById(@RequestParam(required = false) Long id) {
        return Result.success(rbacMenu.searchById(id));
    }

    @GetMapping("/menu/list")
    public Result<List<MenuDTO>> searchList(@RequestParam(required = false) Long systemId) {
        List<MenuDTO> result;
        if(nonNull(systemId)) {
            result = rbacMenu.searchBySystemId(systemId);
        } else {
            result = rbacMenu.all();
        }
        return Result.success(result);
    }

    @GetMapping("/menu/tree")
    public Result<List<Tree<Long>>> searchTree(@RequestParam(required = false) Long systemId) {
        List<MenuDTO> routers = searchList(systemId).getData();
        return Result.success(rbacMenu.toTree(routers));
    }
}
