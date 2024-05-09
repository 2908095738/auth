package com.bbs.stream.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBType {

    STREAM("stream"),
    AUTH("auth");

    private final String dbName;
}