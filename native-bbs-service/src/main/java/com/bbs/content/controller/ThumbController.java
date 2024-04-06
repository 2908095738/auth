package com.bbs.content.controller;

import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.content.cache.ThumbCache;
import com.bbs.content.dto.param.CancelThumbParam;
import com.bbs.content.dto.param.CreateThumbParam;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;

/**
 * 点赞
 */
@RestController
@RequestMapping("/thumb")
public class ThumbController {

    private RabbitmqSend rabbitmqSend;

    private final ThumbCache cache;
    private TransactionDefinition transactionDefinition;
    private DataSourceTransactionManager transactionManager;


    /**
     * 添加点赞,更新用户、内容、评论对应点赞数量:redis
     *
     * @param param param
     * @return Boolean
     */
    @PutMapping
    public Result<Boolean> createThumb(@RequestBody @Valid CreateThumbParam param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
            cache.create(param);
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM, RabbitmqConfig.ROUTINGKEY_AGREE, JSON.toJSONString(param));
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();
    }

    /**
     * 取消点赞
     * @param param param
     * @return Boolean
     */
    @DeleteMapping
    public Result<Boolean> cancelThumb(@RequestBody @Valid CancelThumbParam param) {
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
            //删除点赞数据
    //        param.setUserId(1L);
            //更新用户、内容、评论对应点赞数量:redis
            cache.cancel(param);
            //通知对应的用户
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM, RabbitmqConfig.ROUTINGKEY_DEL_AGREE, JSON.toJSONString(param));
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();

    }


    @Autowired
    public ThumbController(RabbitmqSend rabbitmqSend, ThumbCache cache, TransactionDefinition transactionDefinition, DataSourceTransactionManager transactionManager) {
        this.rabbitmqSend = rabbitmqSend;
        this.cache = cache;
        this.transactionDefinition = transactionDefinition;
        this.transactionManager = transactionManager;
    }

}
