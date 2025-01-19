package com.bbs.auth.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bbs.auth.app.rbac.role.SearchRoleAllMenu;
import com.bbs.auth.entity.RoleMenu;
import com.bbs.auth.entity.SystemRouter;
import com.bbs.auth.entity.rbac.Role;
import com.bbs.auth.entity.rbac.UserRole;
import com.bbs.auth.service.SystemRouterService;
import com.bbs.auth.mapper.SystemRouterMapper;
import com.bbs.auth.service.UserRoleService;
import com.bbs.auth.service.UserService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.*;

/**
* @author 路晨霖
* @description 针对表【system_router(路由：元数据)】的数据库操作Service实现
* @createDate 2024-07-02 19:02:39
*/
@Service
public class SystemRouterServiceImpl extends MPJBaseServiceImpl<SystemRouterMapper, SystemRouter>
    implements SystemRouterService{

    @Resource
    private UserService userService;

    @Override
    public List<SystemRouter> searchBySystemAndUserId(Long systemId, Long userId) {
        return selectJoinList(SystemRouter.class, new MPJLambdaWrapper<SystemRouter>()
                .selectAll(SystemRouter.class)
                .leftJoin(RoleMenu.class, RoleMenu::getMenuId, SystemRouter::getId)
                .leftJoin(UserRole.class, UserRole::getRoleId, RoleMenu::getRoleId)
                .leftJoin(Role.class, Role::getId, UserRole::getRoleId)
                .eq(UserRole::getUserId, userId)
                .eq(Role::getState, INTEGER_ONE)    //启用角色
                .eq(SystemRouter::getSystemId, systemId)
                .eq(SystemRouter::getState, INTEGER_ONE)    //启用菜单
        );
    }

    @Override
    public List<SystemRouter> searchBySystemId(Long systemId) {
        return list(new LambdaQueryWrapper<SystemRouter>()
                .eq(nonNull(systemId), SystemRouter::getSystemId, systemId)
                .eq(userService.loginUserNotIsAdmin(), SystemRouter::getIsAdmin, INTEGER_ZERO)
        );
    }

    @Override
    public List<SystemRouter> searchBySystemAndRoleId(Long systemId, Long roleId) {
        Boolean loginUserNotIsAdmin = userService.loginUserNotIsAdmin();
        return selectJoinList(SystemRouter.class, new MPJLambdaWrapper<SystemRouter>()
                .selectAll(SystemRouter.class)
                .leftJoin(RoleMenu.class, on -> on
                        .eq(RoleMenu::getMenuId, SystemRouter::getId)
                        .eq(RoleMenu::getRoleId, roleId)
                )
                .selectAssociation(RoleMenu.class, SystemRouter::getRoleMenu)
                .eq(SystemRouter::getSystemId, systemId)
                .eq(SystemRouter::getState, INTEGER_ONE)
                .eq(loginUserNotIsAdmin, SystemRouter::getIsAdmin, 0)
        );
    }

    @Override
    public SearchRoleAllMenu.VO searchTreeBySystemAndRoleId(Long systemId, Long roleId) {
        List<SystemRouter> routers = searchBySystemAndRoleId(systemId, roleId);
        List<Long> menuIdList = routers.stream().filter(systemRouter -> nonNull(systemRouter.getRoleMenu())).map(SystemRouter::getId).collect(Collectors.toList());
        return new SearchRoleAllMenu.VO(toTree(routers), menuIdList);
    }

    @Override
    public List<Tree<Long>> toTree(List<SystemRouter> routers) {
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setDeep(5);
        treeNodeConfig.setParentIdKey("parentId");
        treeNodeConfig.setChildrenKey("children");

        return TreeUtil.build(routers, LONG_ZERO, treeNodeConfig, (router, tree) -> {
            if(nonNull(router)){
                tree.setId(router.getId());
                tree.setParentId(router.getParentId());
                tree.setWeight(router.getWeight());
                tree.setName(router.getCode());
                tree.putExtra("code", router.getCode());

                if(isNotBlank(router.getIconName())) {
                    tree.putExtra("iconName", router.getIconName());
                }
                if(isNotBlank(router.getTitle())) {
                    tree.putExtra("title", router.getTitle());
                }
                if(nonNull(router.getType())) {
                    tree.putExtra("type", router.getType());
                }
                if(nonNull(router.getSystemId())) {
                    tree.putExtra("systemId", router.getSystemId());
                }
                if(isNotBlank(router.getPath())) {
                    tree.putExtra("path", router.getPath());
                }
                if(isNotBlank(router.getComponentPath())) {
                    tree.putExtra("componentPath", router.getComponentPath());
                }
            }
        });
    }
}




