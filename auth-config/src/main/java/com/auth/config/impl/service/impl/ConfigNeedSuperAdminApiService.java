package com.auth.config.impl.service.impl;

import com.auth.config.Config;
import com.auth.config.impl.entity.ConfigNeedSuperAdminApi;
import com.auth.config.impl.mapper.ConfigNeedSuperAdminApiMapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConfigNeedSuperAdminApiService extends MPJBaseServiceImpl<ConfigNeedSuperAdminApiMapper, ConfigNeedSuperAdminApi> implements Config.NeedSuperAdminConfig, InitializingBean {

    private static final Set<String> NEED_SUPER_ADMIN_API_PATH_SET = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("需要超管权限 API 配置 - 开始加载配置...");
        List<ConfigNeedSuperAdminApi> list = list();
        Set<String> needSuperAdminApiPathSet = list.stream().map(ConfigNeedSuperAdminApi::getPath).collect(Collectors.toSet());
        NEED_SUPER_ADMIN_API_PATH_SET.addAll(needSuperAdminApiPathSet);
        log.info("需要超管权限 API 配置 - 完成加载！！！数量={}", needSuperAdminApiPathSet.size());
    }

    @Override
    public Boolean isNeedSuperAdmin(String apiPath) {
        return NEED_SUPER_ADMIN_API_PATH_SET.stream().anyMatch(needSuperAdminAPIPath -> {
            if (needSuperAdminAPIPath.endsWith("/*")) {
                return apiPath.startsWith(needSuperAdminAPIPath.substring(0, needSuperAdminAPIPath.length() - 2));
            } else {
                return needSuperAdminAPIPath.equals(apiPath);
            }
        });
    }
}
