package com.auth.user.impl.service.impl;

import com.auth.user.User;
import com.auth.user.dto.UserDTO;
import com.auth.user.entity.UserEntity;
import com.auth.user.impl.converter.UserConverter;
import com.auth.user.impl.mapper.UserMapper;
import com.auth.user.menu.UserStateEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询用户
 * @author ext.luchenlin5
 */
@Slf4j
@Primary
@Service
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, UserEntity> implements User.Search, User.Edit, User.Save {

    @Resource
    private UserConverter converter;

    @Override
    public UserDTO byPhone(String phone) {
        return converter.toDTO(lambdaQuery().eq(UserEntity::getState, UserStateEnum.STATUS_NORMAL).eq(UserEntity::getPhone, phone).one());
    }
    @Override
    public UserDTO byId(Long id) {
        UserEntity entity = super.getById(id);
        return converter.toDTO(entity);
    }

    @Override
    public List<UserDTO> byIds(List<Long> ids) {
        return converter.toDTO(super.listByIds(ids));
    }

    @Override
    public Page<UserDTO> page(Integer current, Integer size) {
        return converter.toDTO(super.page(new Page<>(current, size)));
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
        UserEntity entity = converter.toEntity(userDTO);
        entity.setId(null);
        entity.setCreateBy(null);
        entity.setUpdateBy(null);
        super.save(entity);
    }
}
