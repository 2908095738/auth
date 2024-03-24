package com.auth.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ZKUtil {

    @Resource
    private CuratorFramework client;

    /**
     * 根据路径，获取节点数据
     * @param path 节点路径
     * @return 节点数据
     * @throws RuntimeException 获取异常
     */
    public String getForPath(String path) throws RuntimeException {
        try {
            TimeInterval timer = DateUtil.timer();
            String dataStr = new String(client.getData().forPath(path), StandardCharsets.UTF_8);
            long interval = timer.interval();
            log.debug("Zookeeper： 获取数据 path={}, data={}; 耗时（毫秒）={}", path, dataStr, interval);
            return dataStr;
        } catch (Exception e) {
            log.error("无法从 ZK 获取系统参数配置!!!");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Integer getIntForPath(String path) throws RuntimeException {
        return Integer.valueOf(getForPath(path));
    }

    public Long getLongForPath(String path) throws RuntimeException {
        return Long.valueOf(getForPath(path));
    }

    public Double getDoubleForPath(String path) throws RuntimeException {
        return Double.valueOf(getForPath(path));
    }
}
