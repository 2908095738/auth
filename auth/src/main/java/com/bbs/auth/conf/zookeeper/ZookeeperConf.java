package com.bbs.auth.conf.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Zookeeper Config
 * @see <a href=https://blog.csdn.net/qq_51513626/article/details/135534442>参考</a>
 */
@Configuration
public class ZookeeperConf {

    // 服务器连接地址，集群模式则使用逗号分隔如：ip1:host,ip2:host
    @Value("${apache.zookeeper.connect-url}")
    private String connectUrl;

    // 会话超时时间：单位ms
    @Value("${apache.zookeeper.session-timeout}")
    private Integer sessionTimeout;

    // 连接超时时间：单位ms
    @Value("${apache.zookeeper.connection-timeout}")
    private Integer connectionTimeout;

    // ACL权限控制，验证策略
    @Value("${apache.zookeeper.scheme}")
    private String scheme;

    // 验证内容id
    @Value("${apache.zookeeper.auth_id}")
    private String authId;

    @Resource
    private CuratorRetryPolicy curatorRetryPolicy;

    @Bean
    public CuratorFramework curatorFramework(){
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(connectUrl)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                // 权限认证
                //.authorization(scheme,authId.getBytes(StandardCharsets.UTF_8))
                // 重试策略
                .retryPolicy(new ExponentialBackoffRetry(curatorRetryPolicy.getBaseSleepTime()
                        ,curatorRetryPolicy.getMaxRetries()
                        ,curatorRetryPolicy.getMaxSleep()))
                .build();
        // 启动客户端
        curatorFramework.start();
        return curatorFramework;
    }
}
