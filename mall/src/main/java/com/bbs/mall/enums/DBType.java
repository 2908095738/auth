package com.bbs.mall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBType {

    MALLC("mallc"),

    AUTH("auth");

    private final String dbName;
}