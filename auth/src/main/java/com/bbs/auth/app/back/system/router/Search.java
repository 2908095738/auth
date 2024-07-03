package com.bbs.auth.app.back.system.router;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.Result;
import com.bbs.auth.entity.SystemRouter;
import com.bbs.auth.service.SystemRouterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@RestController("searchSystemRouter")
@RequestMapping
public class Search {

    @Resource
    private SystemRouterService systemRouterService;

    @GetMapping("/back/system/router/list")
    public Result<List<SystemRouter>> searchList(@RequestParam(required = false) Long systemId) {
        return Result.success(systemRouterService.list(new LambdaQueryWrapper<SystemRouter>()
                .eq(nonNull(systemId), SystemRouter::getSystemId, systemId)
        ));
    }

    @GetMapping("/back/system/router")
    public Result<List<Tree<Long>>> searchTree(@RequestParam(required = false) Long systemId) {
        List<SystemRouter> routers = systemRouterService.list(new LambdaQueryWrapper<SystemRouter>()
                .eq(nonNull(systemId), SystemRouter::getSystemId, systemId)
        );

        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setDeep(5);
        treeNodeConfig.setParentIdKey("parentId");
        treeNodeConfig.setChildrenKey("children");

        return Result.success(TreeUtil.build(routers, LONG_ZERO, treeNodeConfig, (router, tree) -> {
            if(nonNull(router)){
                tree.setId(router.getId());
                tree.setParentId(router.getParentId());
                tree.setWeight(router.getWeight());
                tree.setName(router.getCode());
                tree.putExtra("code", router.getCode());

                if(isNotBlank(router.getTitle())) tree.putExtra("title", router.getTitle());
                if(nonNull(router.getType())) tree.putExtra("type", router.getType());
                if(nonNull(router.getSystemId())) tree.putExtra("systemId", router.getSystemId());
                if(isNotBlank(router.getPath())) tree.putExtra("path", router.getPath());
                if(isNotBlank(router.getComponentPath())) tree.putExtra("componentPath", router.getComponentPath());
            }
        }));
    }
}
