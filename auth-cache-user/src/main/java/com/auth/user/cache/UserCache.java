package com.auth.user.cache;

import com.auth.user.dto.UserDTO;

import java.util.List;

public interface UserCache {

    void reload(UserDTO user) throws IllegalArgumentException;

    void remove(Long userId);

    UserDTO get(Long uid);

    List<UserDTO> get(List<Long> ids);

    void expire(Long userId);
}