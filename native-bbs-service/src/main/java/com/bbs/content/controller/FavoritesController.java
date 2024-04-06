package com.bbs.content.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.dto.GetFavoritesDto;
import com.bbs.content.dto.MqFavoritesDto;
import com.bbs.content.dto.param.CreateFavoritesParam;
import com.bbs.content.dto.param.GetFavoritesParam;
import com.bbs.content.entity.Favorites;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import com.bbs.content.service.FavoritesService;
import com.bbs.content.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.Objects;

import static cn.hutool.core.collection.CollUtil.isNotEmpty;

/**
 * 收藏
 */
@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private FavoritesService favoritesService;
    private ThumbCache thumbCache;
    private RabbitmqSend rabbitmqSend;
    private TransactionDefinition transactionDefinition;
    private DataSourceTransactionManager transactionManager;


    /**
     * 添加收藏
     * @param param param
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createFavorites(@RequestBody @Valid CreateFavoritesParam param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Long createId = ThreadLocalUtil.getCurrentUserId();
            Favorites result = favoritesService.create(param,createId);
            if(Objects.isNull(result))return Result.failed("添加失败！");
            //通知
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_FAVORITE, JSON.toJSONString(new MqFavoritesDto(
                    createId, param.getNewId(), new Date()
            )));
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();
    }


    /**
     * 删除收藏
     */
    @DeleteMapping
    public Result<Boolean> delFavorites(@RequestBody @Valid Long id){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Favorites result = favoritesService.delFavorite(id);
            if(Objects.isNull(result))return Result.failed("取消失败！");
            //通知
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_UNFAVORITE, JSON.toJSONString(new MqFavoritesDto(
                    result.getUserId(), result.getNewId(), new Date()
            )));
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();
    }


    /**
     * 查询收藏
     */
    @GetMapping
    public Result<Page<GetFavoritesDto>> getFavorites(GetFavoritesParam param){
        Page<GetFavoritesDto> result = favoritesService.getFavorites(param);
        if(isNotEmpty(result.getRecords()))
            result.getRecords().forEach(o -> o.setLikeCount(thumbCache.countBy(o.getNewId(), null, null, 1)));
        return Result.success(result);
    }



    @Autowired
    public FavoritesController(FavoritesService favoritesService, ThumbCache thumbCache, RabbitmqSend rabbitmqSend, TransactionDefinition transactionDefinition, DataSourceTransactionManager transactionManager) {
        this.favoritesService = favoritesService;
        this.thumbCache = thumbCache;
        this.rabbitmqSend = rabbitmqSend;
        this.transactionDefinition = transactionDefinition;
        this.transactionManager = transactionManager;
    }


}
