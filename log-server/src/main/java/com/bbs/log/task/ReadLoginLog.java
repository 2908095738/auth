package com.bbs.log.task;

import cn.hutool.json.JSONUtil;
import com.bbs.log.entity.LogLogin;
import com.bbs.log.service.LogLoginService;
import com.bbs.log.util.ZKUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RDeque;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@Component
public class ReadLoginLog {

    private static final String LOG_DEQUE_KEY = "LOG:LOGIN";

    private static final String CLEAR_LOG_THRESHOLD = "/conf/log/login/clear/threshold";

    @Resource
    private ZKUtil zkUtil;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private LogLoginService service;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void read() {
        RDeque<String> deque = redissonClient.getDeque(LOG_DEQUE_KEY);
        Integer threshold = zkUtil.getIntForPath(CLEAR_LOG_THRESHOLD);
        log.info("[Task:LoginLog] 日志入库任务开始... Redis Key={}; 读取阈值={};", LOG_DEQUE_KEY, threshold);
        try {
            List<String> logStrList = deque.poll(threshold);

            if(nonNull(logStrList) && logStrList.size() > 0) {
                List<LogLogin> logs = logStrList.stream()
                        .map(log -> JSONUtil.toBean(log, LogLogin.class)).collect(Collectors.toList());

                if(!service.saveBatch(logs)) {
                    log.error("[Task:LoginLog] 日志入库任务失败！！！失败原因={}; Redis Key={}; 读取阈值={};", "请查看日志", LOG_DEQUE_KEY, threshold);
                    return;
                }
            }
            log.info("[Task:LoginLog] 日志入库任务完成！Redis Key={}; 读取阈值={}; 读取数量={}; 队列剩余数量={};",
                    LOG_DEQUE_KEY, threshold, logStrList.size(), deque.size());
        } catch (Exception e) {
            log.error("[Task:LoginLog] 日志入库任务失败！！！失败原因={}; Redis Key={}; 读取阈值={};", e.getMessage(), LOG_DEQUE_KEY, threshold);
            throw new RuntimeException(e);
        }
    }
}
