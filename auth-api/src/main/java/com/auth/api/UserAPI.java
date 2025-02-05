package com.auth.api;

import com.auth.api.dto.UserDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface UserAPI {

    UserDTO getByToken(String token);

    List<UserDTO> list(List<Long> ids);

    List<UserDTO> list(Set<Long> ids);

    List<UserDTO> getByName(String userName);

    Map<Long, UserDTO> getMap(Set<Long> ids);

    void remove(Long id);
}
