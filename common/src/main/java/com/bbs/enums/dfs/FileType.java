package com.bbs.enums.dfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {

    NO_FILE(10, "非文件"),

    IMAGE(11, "图片"),

    VIDEO(12, "视频");

    private final Integer code;

    private final String msg;
}
