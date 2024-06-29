package com.bbs.auth.app.dubbo;

import com.bbs.api.auth.User;
import com.bbs.api.auth.UserAPI;
import com.bbs.auth.cache.BindLoginCompanyCache;
import com.bbs.auth.converter.UserConverter;
import com.bbs.auth.service.TokenService;
import com.bbs.auth.service.UserService;
import com.bbs.vo.UserVO;
import com.bbs.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@DubboService
public class UserAPIImpl implements UserAPI {

    @Resource
    private UserService userService;

    @Resource
    private UserConverter converter;

    @Resource
    private TokenService tokenService;
    @Resource
    private BindLoginCompanyCache bindLoginCompanyCache;

    @Override
    public User getUserByToken(String token) {
        UserVO vo = tokenService.verify(token);
        vo.setCompanyId(bindLoginCompanyCache.get(vo.getId()));
        return converter.toAPIUser(vo);
    }

    @Override
    public List<User> getUserList(List<Long> ids) {
        return converter.toAPIUser(userService.search(ids));
    }

    @Override
    public List<User> getUserList(Set<Long> ids) {
        return converter.toAPIUser(userService.search(ids));
    }

    @Override
    public List<User>  searchByUserOrSave(Long companyId, List<User> userList) {
        List<com.bbs.auth.entity.User> users = userService.searchByUserOrSave(companyId, userList);
        return BeanUtils.toBean(users, User.class);
    }

    @Override
    public User getUserByName(String userName) {
        return converter.toAPIUser(userService.lambdaQuery().eq(com.bbs.auth.entity.User::getName, userName).one());
    }

    @Override
    public Map<Long, User> getUserIdMap(Set<Long> ids) {
        return getUserList(ids).stream().collect(Collectors.toMap(User::getId, user -> user));
    }
}
