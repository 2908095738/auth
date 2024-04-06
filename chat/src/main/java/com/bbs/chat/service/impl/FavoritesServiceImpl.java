package com.bbs.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.chat.entity.ChatTop;
import com.bbs.chat.entity.Favorites;
import com.bbs.chat.entity.News;
import com.bbs.chat.mapper.ChatTopMapper;
import com.bbs.chat.mapper.FavoritesMapper;
import com.bbs.chat.service.FavoritesService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites>
        implements FavoritesService {

    @Autowired
    private ChatTopMapper chatTopMapper;

    @Override
    public Result createFavorites(Favorites favorites) {
        //查询被收藏方id
        MPJLambdaWrapper byIdWrap = new MPJLambdaWrapper<News>()
                .select(News::getCreateId)
                .leftJoin(News.class, News::getNewId, Favorites::getNewId)
                .eq(Favorites::getDeleteFlag, 0)
                .eq(Favorites::getUserId, favorites.getUserId())
                .eq(Favorites::getNewId, favorites.getNewId());
        Map<String, Object> tmpMap = getMap(byIdWrap);

        if (Objects.nonNull(tmpMap)) {
            Long byUserId = (Long) getMap(byIdWrap).get("create_id");

            ChatTop tmpTop = new MPJLambdaWrapper<ChatTop>(ChatTop.class)//查询消息页顶部未读
                    .selectAll(ChatTop.class)
                    .eq(ChatTop::getUserId, byUserId)
                    .one();

            //消息页顶部未读赋值
            if (Objects.nonNull(tmpTop)) {
                Integer nowAgree = tmpTop.getAgreeCount() + 1;
                tmpTop.setAgreeCount(nowAgree);
                chatTopMapper.updateById(tmpTop);
            } else {
                tmpTop = new ChatTop();
                tmpTop.setUserId(byUserId);
                tmpTop.setAgreeCount(1);
                chatTopMapper.insert(tmpTop);
            }
            return Result.success();
        } else {
            return Result.failed("db no data");
        }
    }

    @Override
    public Result cancelFavorites(Favorites favorites) {
        //查询被收藏方id
        MPJLambdaWrapper byIdWrap = new MPJLambdaWrapper<News>()
                .select(News::getCreateId)
                .leftJoin(News.class, News::getNewId, Favorites::getNewId)
                .eq(Favorites::getDeleteFlag, 1)
                .eq(Favorites::getUserId, favorites.getUserId())
                .eq(Favorites::getNewId, favorites.getNewId());
        Map<String, Object> tmpMap = getMap(byIdWrap);

        if (Objects.isNull(tmpMap)) {
            return Result.failed("db data err");
        }

        Long byUserId = (Long) getMap(byIdWrap).get("create_id");
        ChatTop tmpTop = new MPJLambdaWrapper<ChatTop>(ChatTop.class)//查询消息页顶部未读
                .selectAll(ChatTop.class)
                .eq(ChatTop::getUserId, byUserId)
                .one();

        //消息页顶部未读赋值
        if (Objects.nonNull(tmpTop)) {
            Integer nowAgree = tmpTop.getAgreeCount() - 1;
            tmpTop.setAgreeCount(nowAgree);

            int line = chatTopMapper.updateById(tmpTop);
            if (line > 0) {
                return Result.success();
            } else {
                return Result.failed("db err");
            }
        } else {
            return Result.failed("db no data");
        }
    }
}