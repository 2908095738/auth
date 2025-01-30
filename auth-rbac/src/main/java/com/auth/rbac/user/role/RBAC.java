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

/**
 * rbac
 * @author ext.luchenlin5
 */
public interface RBAC {

    interface System {

        Page<SystemDTO> searchSystemPage(Integer current, Integer size);

        Boolean save(SystemDTO system);

        Boolean updateSystemAdmin(Long systemId, Long userId);
    }

    interface Role {

        List<RoleDTO> searchList(String systemCode);

        List<RoleDTO> searchList(String systemCode, Long userId);

        RoleDTO searchById(Long id);
    }

    interface Menu {

        List<MenuDTO> searchBySystemIdAndUserId(Long systemId, Long userId);

        List<MenuDTO> search(Long systemId);

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
