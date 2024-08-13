package com.clinic.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBType {

    ANSWER("answer");

    private final String dbName;
}