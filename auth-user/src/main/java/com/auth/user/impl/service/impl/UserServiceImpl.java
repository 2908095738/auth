package com.auth.user.impl.service.impl;

import com.auth.user.User;
import com.auth.user.dto.UserDTO;
import com.auth.user.entity.UserEntity;
import com.auth.user.impl.mapper.UserMapper;
import com.auth.user.menu.UserStateEnum;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.auth.user.impl.converter.UserConverter.CONVERTER;

/**
 * 查询用户
 * @author ext.luchenlin5
 */
@Slf4j
@Primary
@Service
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, UserEntity> implements User.Search, User.Edit, User.Save {

    @Override
    public UserDTO byPhone(String phone) {
        return CONVERTER.toDTO(lambdaQuery().eq(UserEntity::getState, UserStateEnum.STATUS_NORMAL).eq(UserEntity::getPhone, phone).one());
    }
    @Override
    public UserDTO byId(Long id) {
        UserEntity entity = super.getById(id);
        return CONVERTER.toDTO(entity);
    }

    @Override
    public List<UserDTO> byIds(List<Long> ids) {
        return CONVERTER.toDTO(super.listByIds(ids));
    }

    @Override
    public UserDTO byOpenId(String openId) {
        return null;
    }

    @Override
    public void updateAvatar(Long userId, String avatar) {
        lambdaUpdate().set(UserEntity::getAvatar, avatar).eq(UserEntity::getId, userId).update();
    }

    @Override
    public void bindVXOpenId(Long uid, String openId) {
        lambdaUpdate()
                .eq(UserEntity::getId, uid)
                .set(UserEntity::getOpenId, openId)
                .update();
    }
    @Override
    public void save(UserDTO userDTO) {
        UserEntity entity = CONVERTER.toEntity(userDTO);
        entity.setId(null);
        entity.setCreateBy(null);
        entity.setUpdateBy(null);
        super.save(entity);
    }
}
