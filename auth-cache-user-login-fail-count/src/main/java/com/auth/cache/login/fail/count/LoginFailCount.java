package com.auth.cache.login.fail.count;

public interface LoginFailCount {

    /**
     * 尝试递增登录失败次数，如果为达到失败次数限制，返回 true 否则返回 false
     */
    Boolean tryIncr(Long userId);
}
