package com.auth.phone;

public interface PhoneUserIdMapping {

    Long get(String phone);

    Long get(Long phone);
}
