package com.bbs.auth.api.vx;

import com.bbs.auth.enums.ZookeeperNodePaths;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class GetSecret {

    @Resource
    @Lazy
    private CuratorFramework client;

    public String get() {
        try {
            return new String(client.getData().forPath(ZookeeperNodePaths.VXProgram.SECRET), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("程序异常：无法获取 AppID!!! 请检查 1、Zookeeper相关配置 2、通信可用 3.Zookeeper节点状态...");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
