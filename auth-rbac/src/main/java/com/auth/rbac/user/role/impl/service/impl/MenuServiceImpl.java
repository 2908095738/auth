package com.auth.rbac.user.role.impl.service.impl;

import cn.hutool.core.lang.tree.Tree;
import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.user.role.RBAC;
import com.auth.rbac.user.role.enetity.MenuEntity;
import com.auth.rbac.user.role.enetity.RoleMenuEntity;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE;

@Slf4j
@Primary
@Service
public class MenuServiceImpl extends MPJBaseServiceImpl<MenuMapper, MenuEntity> implements RBAC.Menu {

    @Resource
    private Converter converter;

    @Override
    public List<MenuDTO> searchBySystemIdAndUserId(Long systemId, Long userId) {
        return Collections.emptyList();
    }

    @Override
    public List<MenuDTO> search(Long systemId) {
        return Collections.emptyList();
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
        return Collections.emptyList();
    }
}
