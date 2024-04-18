package com.bbs.chat.service;

import com.bbs.exception.BusinessException;

public interface MessageService {

    /**
     * 发送文本消息
     * @param targetUID 目标用户
     * @param content 内容
     * @throws BusinessException 发送失败
     */
    void sendTextMessage(Long targetUID, String content) throws BusinessException;
}
