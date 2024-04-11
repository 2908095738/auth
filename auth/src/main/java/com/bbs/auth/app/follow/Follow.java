package com.bbs.auth.app.follow;

import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.auth.conf.RabbitmqConfig;
import com.bbs.auth.converter.FanConverter;
import com.bbs.auth.entity.Fan;
import com.bbs.auth.service.FanService;
import com.bbs.auth.service.UserService;
import com.bbs.auth.util.MQUtil;
import com.bbs.entity.UserVO;
import lombok.Data;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 关注
 */
@RestController
@RequestMapping
public class Follow {

    @Resource
    private MQUtil mq;
    @Resource
    private FanService fanService;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private DataSourceTransactionManager transactionManager;
    @Resource
    private UserService userService;
    @Resource
    private FanConverter fanConverter;


    @Data
    public static class Param {
        /**
         * 关注账号id
         */
        private Long followUserId;
    }

    /**
     * 添加关注
     */
    @PutMapping("/follow")
    public Result<Boolean> follow(@RequestBody @Valid Param param){
        UserVO loginUser = userService.loginUser();
        Fan fan = fanConverter.toEntity(param);
        fan.setUserId(loginUser.getId());
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        try {
            fanService.create(fan);
            //发通知
            mq.send(
                    RabbitmqConfig.EXCHANGE_TOPICS_CHAT_INFORM,
                    RabbitmqConfig.ROUTINGKEY_FOLLOW,
                    JSON.toJSONString(fanConverter.toMQ(fan))
            );
            transactionManager.commit(transaction);
            return Result.success();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
        }
        return Result.failedNull();
    }
}
