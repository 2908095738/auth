package com.bbs.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBType {

    CHAT("chat"),
    CONTENT("content"),

    AUTH("auth");

    private final String dbName;
}