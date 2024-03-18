package com.auth.enums;

/**
 * Zookeeper 节点路径
 */
public class ZookeeperNodePaths {

    private ZookeeperNodePaths() {}

    /**
     * 微信小程序
     */
    public static class VXProgram {
        /**
         * 应用 ID
         */
        public static final String APP_ID = "/vx/appid";
        /**
         * 密钥
         */
        public static final String SECRET = "/vx/secret";
    }
}
