package com.auth.log;

import cn.hutool.extra.spring.SpringUtil;
import com.auth.log.entity.LoginLog;
import com.auth.log.impl.LoginLogServiceImpl;
import com.auth.vo.BasePageParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.MDC;
import java.util.Date;

public interface Log {

    class RequestID {
        public static final String REQUEST_ID = "TRACE_ID";

        public static String getRequestID() {
            return MDC.get(REQUEST_ID);
        }
    }

    interface Login {

        /**
         * 添加日志到 Redis 中当
         */
        void add(Long userId, Date loginTime, Long tripTime);

        /**
         * 持久化并清除 Redis 中当天的 登录日志
         */
        Boolean saveAndCleanTodayLogs();

        Page<LoginLog> page(BasePageParam param);

        /**
         * 记录登录日志
         */
        static void record(Long userId, Date loginTime, Long tripTime) {
            LoginLogServiceImpl loginLogService = SpringUtil.getBean(LoginLogServiceImpl.class);
            loginLogService.add(userId, loginTime, tripTime);
        }

        /**
         * 清除 Redis 中当天的 登录日志（并持久化）
         */
        static Boolean clearTodayLogs() {
            LoginLogServiceImpl loginLogService = SpringUtil.getBean(LoginLogServiceImpl.class);
            return loginLogService.saveAndCleanTodayLogs();
        }
    }
}
