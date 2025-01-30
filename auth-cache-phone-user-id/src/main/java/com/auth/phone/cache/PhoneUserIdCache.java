package com.auth.phone.cache;

public interface PhoneUserIdCache {

    Long get(String phone);

    Long get(Long phone);

    void expire(String phoneMapKey);

    void reload(String phone, Long userId);

    void remove(String phone);
}
