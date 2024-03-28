package com.bbs.auth.cache;

import com.bbs.auth.entity.UserGroup;

import java.util.List;

public interface UserPermissionCache {

    List<UserGroup> query(Long uid) throws InterruptedException;

    Boolean userIsAdmin(Long uid) throws InterruptedException;
}
