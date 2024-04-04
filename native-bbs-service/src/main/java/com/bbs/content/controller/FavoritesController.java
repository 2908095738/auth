package com.bbs.content.controller;

import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.content.dto.MqFavoritesDto;
import com.bbs.content.dto.param.CreateFavoritesParam;
import com.bbs.content.dto.param.DelFavoritesParam;
import com.bbs.content.entity.Favorites;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import com.bbs.content.service.FavoritesService;
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
import java.util.List;

/**
 * 收藏
 */
@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    private FavoritesService favoritesService;
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
//            favoritesService.create(param);
            //通知
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_FAVORITE, JSON.toJSONString(new MqFavoritesDto(
                    param.getUserId(), param.getNewId(), new Date()
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
    public Result<Boolean> delFavorite(@RequestBody @Valid DelFavoritesParam param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
//            favoritesService.delFavorite(param);
            //通知
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_UNFAVORITE, JSON.toJSONString(new MqFavoritesDto(
                    param.getUserId(), param.getNewId(), new Date()
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
    public Result<List<Favorites>> getFavorite(){
        Long userId = 1L;

        return null;
    }



    @Autowired
    public FavoritesController(FavoritesService favoritesService, RabbitmqSend rabbitmqSend, TransactionDefinition transactionDefinition, DataSourceTransactionManager transactionManager) {
        this.favoritesService = favoritesService;
        this.rabbitmqSend = rabbitmqSend;
        this.transactionDefinition = transactionDefinition;
        this.transactionManager = transactionManager;
    }
}
