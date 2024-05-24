package com.bbs.api.auth;

import java.util.List;
import java.util.Set;

public interface UserAPI {

    User getLoginUser();

    User getUserByToken(String token);

    List<User> getUserList(String token, List<Long> ids);

    List<User> getUserList(List<Long> ids);

    List<User> getUserList(Set<Long> ids);

    List<User> searchByUserOrSave(Long companyId, List<User> userList);
}
