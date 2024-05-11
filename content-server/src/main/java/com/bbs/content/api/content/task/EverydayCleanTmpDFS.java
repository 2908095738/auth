package com.bbs.content.api.content.task;

import cn.hutool.core.date.DateUtil;
import com.bbs.api.DFS;
import com.bbs.content.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import static com.bbs.content.enums.RedisKeys.CONTENT_FILE_UPLOAD_TMP;
import static com.bbs.content.enums.RedisKeys.CONTENT_TMP_FILE_CLEAN;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

/**
 * 每天清除 DFS 临时文件
 */
@Component
public class EverydayCleanTmpDFS {

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> protoStuffTemplate;

    @Resource
    private DFS.Clean clean;

    @Resource
    private RedissonClient redisson;

    @Scheduled(cron = "0 0 1 * * ?")
    public void clean() {
        RedisUtil.Redisson.lockExec(redisson.getLock(CONTENT_TMP_FILE_CLEAN.LOCK.key()), 500, 1000, MILLISECONDS, () -> {
            String key = CONTENT_TMP_FILE_CLEAN.key();
            String cleanFlag = protoStuffTemplate.opsForValue().get(key);
            String yesterday = DateUtil.format(DateUtil.offsetDay(new Date(), -1), "yyyy-MM-dd HH:mm:ss");
            //确保清除操作，每日一次
            if(StringUtils.isNotBlank(cleanFlag) && yesterday.equals(cleanFlag)) {
                Set<String> need = protoStuffTemplate.opsForSet().members(CONTENT_FILE_UPLOAD_TMP.key());   // 从缓存中获取昨日的临时文件
                if(nonNull(need) && need.size() > INTEGER_ZERO) {
                    clean.batchClean(new ArrayList<>(need));    //调用 DFS 服务，执行清除
                    protoStuffTemplate.opsForValue().set(key, DateUtil.now());  //设置清楚标识
                }
            }
        });
    }
}
