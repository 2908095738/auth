package com.bbs.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.auth.entity.Role;
import com.bbs.auth.service.RoleResourceService;
import com.bbs.auth.service.RoleService;
import com.bbs.auth.service.SystemService;
import com.bbs.vo.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {


    @Resource
    private RoleService roleService;

    @Resource
    private RoleResourceService roleResourceService;

    @Resource
    private SystemService systemService;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class QueryRoleParam extends BaseParam {

        private Long id;

        private Long groupId;

    }

    /**
     * 查询所有角色
     */
    @GetMapping
    public Result<Role> queryRole(Role param){
        return roleService.search(param);
    }

    @GetMapping("/page")
    public Result<Page<Role>> queryRolePage(@RequestParam Integer current, @RequestParam Integer size){
        return Result.success(roleService.searchJoinSystem(current, size));
    }


    /**
     * 添加/修改角色
     */
    @PostMapping
    public Result<Boolean> addRole(@RequestBody Role role){
        return Result.success(roleService.saveOrUpdate(role));
    }

    /**
     * 删除角色
     */
    @DeleteMapping
    public Result<Boolean> deleteRole(@RequestBody Long id){
        return Result.success(roleService.updateById(new Role().setId(id).setState(1)));
    }

    @Data
    public static class AddResourcePermissionParam {

        @NotNull
        private Long roleId;

        @NotNull
        private List<com.bbs.auth.entity.Resource> resources;
    }

    /**
     * 添加资源权限
     */
    @PutMapping("/resource/batch")
    public Result<Boolean> addResourcePermission(@RequestBody @Valid AddResourcePermissionParam param){
        return roleResourceService.update(param);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class SearchResourcePermissionParam extends BaseParam {
        @NotNull
        private Long roleId;
    }

    /**
     * 查询资源权限
     */
    @GetMapping("/resource")
    public Result<Page<com.bbs.auth.entity.Resource>> searchResourcePermission(@Valid SearchResourcePermissionParam param){
        return roleResourceService.search(param);
    }
}
