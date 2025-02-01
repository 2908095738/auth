package com.auth.config.interceptor.login;

import com.auth.user.entity.LoginInterceptIgnoreConfig;
import com.auth.user.impl.service.LoginInterceptIgnoreConfigService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 项目启动时，加载需要忽略的路径
 * @author ext.luchenlin5
 */
@Slf4j
@Component
public class IgnoreConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private LoginInterceptIgnoreConfigService db;

    private static final Set<String> IGNORE_API_PATHS = new HashSet<>();

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        log.info("忽略登录拦截 - 开始加载登录拦截忽略路径...");
        for (LoginInterceptIgnoreConfig config : db.list()) {
            log.info("忽略登录拦截 - 添加忽略：path={}; class={}", config.getApiPath(), config.getClassPath());
            IGNORE_API_PATHS.add(config.getApiPath());
        }
        log.info("忽略登录拦截 - 加载完成！！！忽略路径数量：{}", IGNORE_API_PATHS.size());
    }

    public static Boolean match(String apiPath) {
        for (String ignoreApiPath : IGNORE_API_PATHS) {
            if(ignoreApiPath.contains(apiPath)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getIgnoreApiPaths() {
        return new ArrayList<>(IGNORE_API_PATHS);
    }
}