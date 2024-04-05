package com.bbs.enums.dfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DSF 服务存储类型
 */
@Getter
@AllArgsConstructor
public enum ResourceType {

    DIR(0, "目录"),  //目录

    FILE(1, "文件");    //文件

    private final Integer code;

    private final String msg;
}
