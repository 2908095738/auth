package com.bbs.chat.converter;

import com.bbs.chat.dto.ChatRecordDto;
import com.bbs.chat.dto.param.CreateChatParam;
import com.bbs.chat.entity.Chat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatConverter {
    Chat toEntity(CreateChatParam param);

    ChatRecordDto toDTO(Chat chat);
}