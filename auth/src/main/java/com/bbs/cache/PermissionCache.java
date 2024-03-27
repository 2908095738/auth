package com.bbs.cache;

import java.util.List;

public interface PermissionCache {


    interface UserGroup {

        void reload();

        List<com.bbs.entity.UserGroup> search(Long uid);
    }


    interface RoleGroup {

        void reload();

        List<com.bbs.entity.RoleGroup> search(List<Long> groupIds);
    }

    interface RoleResource {

        void reload();

        List<com.bbs.entity.RoleResource> search(List<Long> roleIds);
    }
}
