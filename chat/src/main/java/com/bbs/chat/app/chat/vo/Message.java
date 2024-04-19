package com.bbs.chat.app.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private String code;

    private String message;

    private List<String> messages;

    /**
     * 消息数量
     */
    private Integer size;

    public Message(String code, String message, Integer size) {
        this.code = code;
        this.message = message;
        this.size = size;
    }
}
