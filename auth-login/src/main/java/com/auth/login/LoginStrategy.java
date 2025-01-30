package com.auth.login;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoginStrategy implements InitializingBean {

    @Resource
    private ApplicationContext applicationContext;

    private static Map<String, AbstractLoginStrategy> loginStrategyMapping = null;
    @Override
    public void afterPropertiesSet() {
        Map<String, AbstractLoginStrategy> loginStrategyMap = applicationContext.getBeansOfType(AbstractLoginStrategy.class);
        loginStrategyMapping = loginStrategyMap.values()
                .stream().collect(Collectors.toMap(AbstractLoginStrategy::getLoginTypeCode, type -> type));
        log.info("登录策略 - 加载登录策略：{}", JSONUtil.toJsonStr(loginStrategyMapping));
    }

    /**
     * 获取登录类型对应的登录策略
     * @param loginTypeCode 登录类型编码
     * @return 登录策略
     */
    public AbstractLoginStrategy getInstance(String loginTypeCode) {
        return loginStrategyMapping.get(loginTypeCode);
    }
}
