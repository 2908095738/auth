package com.auth.api.impl;

import com.auth.api.UserAPI;
import com.auth.api.converter.UserAPIConverter;
import com.auth.api.dto.UserDTO;
import com.auth.token.Token;
import com.auth.token.impl.dto.UserLoginToken;
import com.auth.token.impl.exception.UserTokenParseException;
import com.auth.user.User;
import com.auth.user.cache.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
@Service
@DubboService
public class UserAPIImpl implements UserAPI {

    @Resource
    private UserAPIConverter converter;

    @Resource
    private User.Search searchUser;

    @Resource
    private User.Delete deleteUser;

    @Resource
    private Token.ParseUserLoginToken parseUserLoginToken;

    @Resource
    private UserCache userCache;

    @Override
    public UserDTO getByToken(String token) throws UserTokenParseException {
        UserLoginToken userLoginToken = parseUserLoginToken.parse(token);
        Long uid = userLoginToken.getUid();
        com.auth.user.dto.UserDTO userDTO = userCache.get(uid);
        if(isNull(userDTO)) {
            userDTO = searchUser.byId(uid);
        }
        return converter.toDTO(userDTO);
    }

    @Override
    public List<UserDTO> list(List<Long> ids) {
        return list(new HashSet<>(ids));
    }

    @Override
    public List<UserDTO> list(Set<Long> ids) {  // hash o(1)  10
        List<com.auth.user.dto.UserDTO> userDTOS = userCache.get(ids);  // 6
        if(userDTOS.size() < ids.size()) {
            Set<Long> copyIds = new HashSet<>(ids);
            userDTOS.forEach(userDTO -> copyIds.remove(userDTO.getId()));
            userDTOS.addAll(searchUser.byIds(copyIds));
            return userDTOS.stream()
                    .map(converter::toDTO)
                    .sorted(Comparator.comparing(UserDTO::getId)).collect(Collectors.toList());
        } else {
            return converter.toDTO(userDTOS);
        }
    }

    @Override
    public List<UserDTO> getByName(String userName) {
        return converter.toDTO(searchUser.byName(userName));
    }

    @Override
    public Map<Long, UserDTO> getMap(Set<Long> ids) {
        return list(ids).stream().collect(Collectors.toMap(UserDTO::getId, userDTO -> userDTO));
    }

    @Override
    public void remove(Long id) {
        deleteUser.del(id);
    }
}
