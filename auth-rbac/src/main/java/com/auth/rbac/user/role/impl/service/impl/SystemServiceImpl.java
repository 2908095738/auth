package com.auth.rbac.user.role.impl.service.impl;

import com.auth.rbac.dto.SystemDTO;
import com.auth.rbac.user.role.RBAC;
import com.auth.rbac.user.role.enetity.SystemEntity;
import com.auth.rbac.user.role.impl.converter.Converter;
import com.auth.rbac.user.role.impl.mapper.SystemMapper;
import com.auth.user.User;
import com.auth.user.dto.UserDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@Primary
@Service
public class SystemServiceImpl extends MPJBaseServiceImpl<SystemMapper, SystemEntity> implements RBAC.System {

    @Resource
    private Converter converter;

    @Resource
    private User.Search searchUser;

    @Override
    public Page<SystemDTO> searchSystemPage(Integer current, Integer size) {
        Page<SystemEntity> page = lambdaQuery().eq(SystemEntity::getState, NumberUtils.INTEGER_ZERO)
                .page(new Page<>(current, size));

        Page<SystemDTO> result = converter.toSystemDTOPage(page);
        fillUserInfo(result.getRecords());
        return result;
    }

    @Override
    public List<SystemDTO> searchSystem() {
        List<SystemEntity> list = lambdaQuery().eq(SystemEntity::getState, NumberUtils.INTEGER_ZERO).list();
        List<SystemDTO> result = converter.toSystemDTOList(list);
        fillUserInfo(result);
        return result;
    }

    @Override
    public Map<String, SystemDTO> searchBySystemCode(Set<String> systemCodes) {
        List<SystemEntity> list = lambdaQuery().in(SystemEntity::getCode, systemCodes).list();
        return list.stream().collect(Collectors.toMap(SystemEntity::getCode, converter::toSystemDTO));
    }

    private void fillUserInfo(List<SystemDTO> systemList) {
        List<Long> userIds = new ArrayList<>();
        systemList.forEach(system ->{
            if(nonNull(system.getAdminId())) {
                userIds.add(system.getAdminId());
            }
            if(nonNull(system.getCreateBy())) {
                userIds.add(system.getCreateBy());
            }
            if(nonNull(system.getUpdateBy())) {
                userIds.add(system.getUpdateBy());
            }
            if(nonNull(system.getUpdateBy())) {
                userIds.add(system.getUpdateBy());
            }
        });
        Map<Long, UserDTO> mapping = searchUser.byIds(userIds).stream().collect(Collectors.toMap(UserDTO::getId, dto -> dto));
        systemList.forEach(system ->{
            UserDTO adminUserInfo = mapping.get(system.getAdminId());
            system.setAdmin(adminUserInfo);
            UserDTO createUser = mapping.get(system.getCreateBy());
            system.setCreateUser(createUser);
            UserDTO updateUser = mapping.get(system.getUpdateBy());
            system.setUpdateUser(updateUser);
        });
    }

    @Override
    public Boolean save(SystemDTO system) {
        return super.save(converter.toEntity(system));
    }

    @Override
    public Boolean updateSystemAdmin(Long systemId, Long userId) {
        return lambdaUpdate().set(SystemEntity::getAdminId, userId).eq(SystemEntity::getId, systemId).update();
    }
}
