package com.bbs.enums.dfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {

    IMAGE(1, "图片"),

    VIDEO(2, "视频");

    private final Integer code;

    private final String msg;
}
