package com.auth.rbac.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuType {

    PAGE(0, "页面"),
    DIALOG(1, "弹窗"),
    MODULE(2, "组件"),
    ;

    private final Integer code;

    private final String desc;
}
