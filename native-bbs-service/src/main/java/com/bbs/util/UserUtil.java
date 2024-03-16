package com.bbs.util;

public class UserUtil {

    public static Long loginUserID() {
        return ThreadLocalUtil.getCurrentUser().getId();
    }
}
