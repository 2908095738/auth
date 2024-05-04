package com.bbs.content.controller;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.content.dto.GetFollowOrFanDto;
import com.bbs.content.dto.MqFollowDto;
import com.bbs.content.dto.param.CreateFollowParam;
import com.bbs.content.dto.param.DelFollowParam;
import com.bbs.content.entity.Fan;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import com.bbs.content.service.FanService;
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
import java.util.List;

/**
 * 关注
 */
@RestController
@RequestMapping()
public class FollowController {

//    private FanCache fanCache;
    private RabbitmqSend rabbitmqSend;
    private FanService fanService;
    private TransactionDefinition transactionDefinition;
    private DataSourceTransactionManager transactionManager;

    /**
     * 添加关注
     * @param param param
     * @return Boolean
     */
    @PutMapping("/follow")
    public Result<Boolean> createFollow(@RequestBody @Valid CreateFollowParam param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            param.setUserId(userId);
            fanService.create(param);
            //发通知
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_FOLLOW, JSON.toJSONString(new MqFollowDto(
                param.getUserId(),param.getFollowUserId(),new Date()
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
     * 查询关注
     * @return Boolean
     */
    @GetMapping("/follow")
    public Result<List<GetFollowOrFanDto>> getFollow(){
        Long userId = ThreadLocalUtil.getCurrentUserId();
        List<Fan> list = fanService.getFollow(userId);
        if(CollUtil.isNotEmpty(list)){
//            List<GetFollowOrFanDto> result = list.stream().map(o -> new GetFollowOrFanDto(o.getFollowUserId(), , )).collect(Collectors.toList());
//            return Result.success(result);
        }
        return Result.failedNull();
    }


    /**
     * 查询粉丝
     * @return Boolean
     */
    @GetMapping("/fan")
    public Result<List<GetFollowOrFanDto>> getFan(){
        Long userId = ThreadLocalUtil.getCurrentUserId();
        List<Fan> list = fanService.getFollow(userId);
        if(CollUtil.isNotEmpty(list)){
//            List<GetFollowOrFanDto> result = list.stream().map(o -> new GetFollowOrFanDto(o.getUserId(), , )).collect(Collectors.toList());
//            return Result.success(result);
        }
        return Result.failedNull();
    }


    /**
     * 删除关注
     */
    @DeleteMapping("/follow")
    public Result<Boolean> delFollow(@RequestBody @Valid DelFollowParam param){
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            fanService.delFollow(param);
            //发通知
            rabbitmqSend.send(RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,RabbitmqConfig.ROUTINGKEY_UNFOLLOW, JSON.toJSONString(new MqFollowDto(
                    param.getUserId(),param.getFollowUserId(),new Date()
            )));
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();
    }

    @Autowired
    public FollowController(RabbitmqSend rabbitmqSend, FanService fanService, TransactionDefinition transactionDefinition, DataSourceTransactionManager transactionManager) {
        this.fanService = fanService;
        this.rabbitmqSend = rabbitmqSend;
        this.transactionDefinition = transactionDefinition;
        this.transactionManager = transactionManager;
    }
}
