package com.bbs.file.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fdfs")
public class FastDFSConf {

    /**
     * fastdfs对外域名
     */
    private String outurl;
}
