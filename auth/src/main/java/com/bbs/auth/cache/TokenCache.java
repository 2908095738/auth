package com.bbs.auth.cache;

public interface TokenCache {

    void setToken(Long uid, String token);

    String getToken(Long uid);

    void expireToken(Long uid);
}
