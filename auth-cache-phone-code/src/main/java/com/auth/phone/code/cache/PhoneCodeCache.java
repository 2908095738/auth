package com.auth.phone.code.cache;

public interface PhoneCodeCache {

    void reload(String phone, Integer code) throws IllegalArgumentException;

    void remove(String phone);

    Integer get(String phone);

    void expire(String phone);
}
