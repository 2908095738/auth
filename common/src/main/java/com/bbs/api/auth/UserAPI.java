package com.bbs.api.auth;

import java.util.List;

public interface UserAPI {

    User getLoginUser();

    User getUserByToken(String token);

    List<User> getUserList(String token, List<Long> ids);

    List<User> getUserList(List<Long> ids);
}
