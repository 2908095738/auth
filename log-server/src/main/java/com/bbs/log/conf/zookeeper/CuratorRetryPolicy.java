package com.bbs.log.conf.zookeeper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Zookeeper 重试策略参数
 * @see <a href=https://blog.csdn.net/qq_51513626/article/details/135534442>参考</a>
 */
@ConfigurationProperties(prefix = "apache.retry-policy")
@Configuration
@Getter
@Setter
public class CuratorRetryPolicy {

    // 初始化间隔时间
    private Integer baseSleepTime;

    // 最大重试次数
    private Integer maxRetries;

    // 最大重试间隔时间
    private Integer maxSleep;
}
