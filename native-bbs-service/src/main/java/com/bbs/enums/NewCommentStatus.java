package com.bbs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *状态 10.待审核 20.已发布 110.待审核用户删除 120.已发布用户删除 100010.待审核管理员删除 100020.已发布管理员删除
 */
@Getter
@AllArgsConstructor
public enum NewCommentStatus {

    WAIT_FOR_REVIEW(10, "待审核"),
    HAVE_RELEASED(20, "已发布"),
    USER_PENDING_DELETION(110, "待审核用户删除"),
    USER_PUBLISHED_DELETE(120, "已发布用户删除"),
    ADMIN_PENDING_DELETION(100010, "待审核管理员删除"),
    ADMIN_PUBLISHED_DELETE(100020, "已发布管理员删除"),

    ;

    private final int code;

    private final String description;
}
