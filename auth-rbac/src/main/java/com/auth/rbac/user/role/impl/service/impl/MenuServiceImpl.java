package com.auth.rbac.user.role.impl.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.user.role.RBAC;
import com.auth.rbac.user.role.enetity.MenuEntity;
import com.auth.rbac.user.role.enetity.RoleMenuEntity;
import com.auth.rbac.user.role.enetity.SystemEntity;
import com.auth.rbac.user.role.impl.converter.Converter;
import com.auth.rbac.user.role.impl.mapper.MenuMapper;
import com.auth.user.User;
import com.auth.user.menu.UserSuperAdminEnum;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ZERO;

@Slf4j
@Primary
@Service
public class MenuServiceImpl extends MPJBaseServiceImpl<MenuMapper, MenuEntity> implements RBAC.Menu {

    @Resource
    private Converter converter;

    @Override
    public Boolean add(MenuDTO menuDTO) {
        MenuEntity menuEntity = converter.toMenuEntity(menuDTO);
        Long count = lambdaQuery()
                .eq(MenuEntity::getParentId, menuEntity.getParentId())
                .count();
        menuEntity.setWeight(count +1);
        return super.save(menuEntity);
    }

    @Override
    public Boolean del(Long id) {
        return removeById(id);
    }

    @Override
    public Boolean updateById(MenuDTO menuDTO) {
        return super.updateById(converter.toMenuEntity(menuDTO));
    }

    @Override
    public Boolean updateTypeById(List<Long> ids, Integer type) {
        return lambdaUpdate()
                .set(MenuEntity::getType, type)
                .in(MenuEntity::getId, ids)
                .update();
    }

    @Override
    public List<MenuDTO> all() {
        return converter.toMenuDTO(list());
    }

    @Override
    public List<MenuDTO> searchBySystemId(Long systemId) {
        return converter.toMenuDTO(lambdaQuery().eq(MenuEntity::getSystemId, systemId).list());
    }

    @Override
    public MenuDTO searchById(Long id) {
        return converter.toMenuDTO(selectJoinOne(MenuEntity.class, new MPJLambdaWrapper<MenuEntity>()
                .selectAll(MenuEntity.class)
                .leftJoin(SystemEntity.class, SystemEntity::getId, MenuEntity::getSystemId, ext -> ext
                        .selectAssociation(System.class, MenuEntity::getSystem)
                )
                .leftJoin(MenuEntity.class, "t2", MenuEntity::getId, MenuEntity::getParentId, ext -> ext
                        .selectAssociation("t2", MenuEntity.class, MenuEntity::getParent)
                )
                .eq(MenuEntity::getId, id)
        ));
    }

    @Override
    public List<MenuDTO> searchBySystemIdAndRoleId(Long systemId, Long roleId) {
        return converter.toMenuDTO(selectJoinList(MenuEntity.class, new MPJLambdaWrapper<MenuEntity>()
                .selectAll(MenuEntity.class)
                .leftJoin(RoleMenuEntity.class, on -> on
                        .eq(RoleMenuEntity::getMenuId, MenuEntity::getId)
                        .eq(RoleMenuEntity::getRoleId, roleId)
                )
                .selectAssociation(RoleMenuEntity.class, MenuEntity::getRoleMenu)
                .eq(MenuEntity::getSystemId, systemId)
                .eq(MenuEntity::getState, INTEGER_ONE)
                .eq(User.LoginUserUtil.loginUserNotIsAdmin(), MenuEntity::getIsAdmin, UserSuperAdminEnum.NOT_IS_SUPER_ADMIN)
        ));
    }

    @Override
    public SearchTreeBySystemAndRoleIdVO searchTree(Long systemId, Long roleId) {
        List<MenuDTO> routers = searchBySystemIdAndRoleId(systemId, roleId);
        List<Long> menuIdList = routers.stream().filter(systemRouter -> nonNull(systemRouter.getRoleMenu())).map(MenuDTO::getId).collect(Collectors.toList());
        return new SearchTreeBySystemAndRoleIdVO(toTree(routers), menuIdList);
    }

    @Override
    public List<Tree<Long>> toTree(List<MenuDTO> routers) {
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
