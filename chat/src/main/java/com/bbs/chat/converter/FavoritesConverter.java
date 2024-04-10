package com.bbs.chat.converter;

import com.bbs.chat.dto.MqFavoritesDto;
import com.bbs.chat.entity.Favorites;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FavoritesConverter {
    Favorites toEntity(MqFavoritesDto param);
}