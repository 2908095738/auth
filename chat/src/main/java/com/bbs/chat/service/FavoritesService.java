package com.bbs.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.chat.entity.Favorites;

public interface FavoritesService extends IService<Favorites> {

    Result createFavorites(Favorites favorites);

    Result cancelFavorites(Favorites favorites);
}