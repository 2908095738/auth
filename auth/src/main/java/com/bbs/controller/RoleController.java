package com.bbs.controller;

import com.bbs.service.RoleResourceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.entity.Role;
import com.bbs.service.RoleService;
import com.bbs.entity.vo.BaseParam;
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


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class QueryRoleParam extends BaseParam {

        private Long groupId;
    }

    /**
     * 查询所有角色
     */
    @GetMapping
    public Result<Page<Role>> queryRole(QueryRoleParam param){
        return roleService.search(param);
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
        private List<com.bbs.entity.Resource> resources;
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
    public Result<Page<com.bbs.entity.Resource>> searchResourcePermission(@Valid SearchResourcePermissionParam param){
        return roleResourceService.search(param);
    }
}
