package com.bbs.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.content.dto.GetFavoritesDto;
import com.bbs.content.dto.param.CreateFavoritesParam;
import com.bbs.content.dto.param.GetFavoritesParam;
import com.bbs.content.entity.Favorites;

/**
 *
 */
public interface FavoritesService extends IService<Favorites> {
    Favorites create(CreateFavoritesParam param, Long createId);
    Favorites delFavorite(Long id);
    Page<GetFavoritesDto> getFavorites(GetFavoritesParam param);


}
