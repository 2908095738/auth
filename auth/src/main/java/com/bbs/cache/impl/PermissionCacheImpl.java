package com.bbs.cache.impl;

import com.bbs.cache.PermissionCache;
import com.bbs.service.RoleGroupService;
import com.bbs.service.RoleResourceService;
import com.bbs.service.UserGroupService;
import com.bbs.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bbs.enums.RedisKeys.*;

@Slf4j
@Component
public class PermissionCacheImpl implements PermissionCache {

    @Slf4j
    @Component
    public static class UserGroupCacheImpl implements UserGroup {

        @javax.annotation.Resource
        private UserGroupService service;

        @javax.annotation.Resource
        private RedisUtil redis;

        private final String key = PERMISSION_USER_GROUP.key();

        @Override
        public void reload() {
        }

        @Override
        public List<com.bbs.entity.UserGroup> search(Long uid) {
            return null;
        }
    }

    @Slf4j
    @Component
    public static class RoleGroupCacheImpl implements RoleGroup {

        @javax.annotation.Resource
        private RoleGroupService service;

        private final String key = PERMISSION_ROLE_GROUP.key();

        @Override
        public void reload() {

        }

        @Override
        public List<com.bbs.entity.RoleGroup> search(List<Long> groupIds) {
            return null;
        }
    }

    @Slf4j
    @Component
    public static class RoleResourceCacheImpl implements RoleResource {

        @javax.annotation.Resource
        private RoleResourceService service;

        private final String key = PERMISSION_ROLE_RESOURCE.key();

        @Override
        public void reload() {

        }

        @Override
        public List<com.bbs.entity.RoleResource> search(List<Long> roleIds) {
            return null;
        }
    }
}
