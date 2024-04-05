package com.bbs.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.content.dto.GetFavoritesDto;
import com.bbs.content.dto.param.CreateFavoritesParam;
import com.bbs.content.dto.param.GetFavoritesParam;
import com.bbs.content.entity.Favorites;
import com.bbs.content.entity.News;
import com.bbs.content.mapper.FavoritesMapper;
import com.bbs.content.service.FavoritesService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class FavoritesServiceImpl extends MPJBaseServiceImpl<FavoritesMapper, Favorites>
    implements FavoritesService {


    @Override
    public Favorites create(CreateFavoritesParam param) {
        Favorites favorites = new Favorites().setNewId(param.getNewId()).setUserId(param.getUserId());
        boolean save = save(favorites);
        if(save){
            return favorites;
        }
        return null;
    }

    @Override
    public Favorites delFavorite(Long id) {
        Favorites one = getById(id);
        boolean b = updateById(one.setDeleteFlag(1));
        if(b){
            return one;
        }
        return null;
    }

    @Override
    public Page<GetFavoritesDto> getFavorites(GetFavoritesParam param) {
        Page<GetFavoritesDto> result = selectJoinListPage(new Page<>(param.getCurrent(), param.getSize()), GetFavoritesDto.class, new MPJLambdaWrapper<Favorites>()
                .selectAll(Favorites.class)
                .selectAs(News::getTitle, GetFavoritesDto::getTitle)
                .selectAs(News::getUserName, GetFavoritesDto::getUserName)
                .selectAs(News::getSummary, GetFavoritesDto::getSummary)
                .eq(Favorites::getDeleteFlag,0)
                .eq(Favorites::getUserId,param.getUserId())
                .like(StringUtils.isNotBlank(param.getTitle()),News::getTitle,param.getTitle())
                .like(StringUtils.isNotBlank(param.getUserName()),News::getUserName,param.getUserName())
                .leftJoin(News.class, News::getNewId, Favorites::getNewId)
        );
        return result;
    }


}




