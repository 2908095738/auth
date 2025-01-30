package com.auth.user.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserSuperAdminEnum {

    NOT_IS_SUPER_ADMIN("普通用户", 0),

    SUPER_ADMIN("超级管理员", 1),
    ;

    private final String name;

    private final Integer value;
}
