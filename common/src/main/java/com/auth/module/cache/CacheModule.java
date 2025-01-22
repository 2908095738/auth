package com.auth.module.cache;

public interface CacheModule {

    String cachePrefix();

    default String cacheKey(String mark) {
        return cachePrefix() + mark;
    };

    String cacheDescription();

    Long cacheTimeout();

    void cacheAdd();

    void cacheRemove();
}
