package com.auth.rbac.user.role;

import cn.hutool.core.lang.tree.Tree;
import com.auth.rbac.dto.MenuDTO;
import com.auth.rbac.dto.RoleDTO;
import com.auth.rbac.dto.SystemDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * rbac
 * @author ext.luchenlin5
 */
public interface RBAC {

    interface System {

        Page<SystemDTO> searchSystemPage(Integer current, Integer size);

        List<SystemDTO> searchSystem();

        Map<String, SystemDTO> searchBySystemCode(Set<String> systemCodes);

        Boolean save(SystemDTO system);

        Boolean updateSystemAdmin(Long systemId, Long userId);
    }

    interface Role {

        List<RoleDTO> searchList(String systemCode);

        List<RoleDTO> searchList(String systemCode, Long userId);

        RoleDTO searchById(Long id);

        Page<RoleDTO> page(Integer current, Integer size);
    }

    interface Menu {

        Boolean add(MenuDTO menuDTO);

        Boolean del(Long id);

        Boolean updateById(MenuDTO menuDTO);

        Boolean updateTypeById(List<Long> ids, Integer type);

        List<MenuDTO> all();

        List<MenuDTO> searchBySystemId(Long systemId);

        MenuDTO searchById(Long id);

        List<MenuDTO> searchBySystemIdAndRoleId(Long systemId, Long roleId);

        SearchTreeBySystemAndRoleIdVO searchTree(Long systemId, Long roleId);

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        class SearchTreeBySystemAndRoleIdVO {

            private List<Tree<Long>> tree;

            private List<Long> roleMenuIds;
        }

        List<Tree<Long>> toTree(List<MenuDTO> routers);
    }

    interface UseRole {

        Boolean add(String systemCode, Long userId, List<Long> roleIds);

        Boolean remove(String systemCode, Long userId);
    }

    interface RoleMenu {

        Boolean removeByRoleId(Long roleId);

        Boolean save(Long roleId, List<Long> menuIds);
    }
}
