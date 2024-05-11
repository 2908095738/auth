package com.bbs.api.auth;

import java.util.List;

public interface UserService {

    User getLoginUser();

    User getUserByToken(String token);

    User getUserByID(Long id);

    List<User> getUserList(String token, List<Long> ids);

    List<User> getUserList(List<Long> ids);
}
