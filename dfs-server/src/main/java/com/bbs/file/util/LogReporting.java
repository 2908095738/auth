package com.bbs.file.util;

import cn.hutool.json.JSONUtil;
import com.bbs.enums.log.MQ;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 日志上报
 */
@Component
public class LogReporting {

    private static final String ROUTE_KEY_DELIMITER = ".";

    @Value("${spring.application.name}")
    private String appName;
    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final String WARN_LOG_ROUTE_KEY = "log.warn";

    private static final String ERROR_LOG_ROUTE_KEY = "log.warn";

    public void error(Object data) {
        error(JSONUtil.toJsonPrettyStr(data));
    }

    public void error(String dataJsonStr) {
        rabbitTemplate.convertAndSend(MQ.EXCHANGE_NAME, appName + ROUTE_KEY_DELIMITER + ERROR_LOG_ROUTE_KEY, dataJsonStr);
    }

    public void warn(Object data) {
        warn(JSONUtil.toJsonPrettyStr(data));
    }

    public void warn(String dataJsonStr) {
        rabbitTemplate.convertAndSend(MQ.EXCHANGE_NAME, appName + ROUTE_KEY_DELIMITER + WARN_LOG_ROUTE_KEY, dataJsonStr);
    }
}
