package com.bbs.chat.converter;

import com.bbs.api.Auth;
import com.bbs.chat.app.chat.vo.UnreadMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageConverter {

    UnreadMessage toUnreadMSG(Auth.UserAPI.User user);
}
