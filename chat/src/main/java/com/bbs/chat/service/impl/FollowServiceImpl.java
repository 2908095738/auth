package com.bbs.chat.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.chat.entity.ChatTop;
import com.bbs.chat.entity.Fan;
import com.bbs.chat.enums.DBType;
import com.bbs.chat.mapper.ChatTopMapper;
import com.bbs.chat.mapper.FanMapper;
import com.bbs.chat.service.FollowService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FollowServiceImpl extends ServiceImpl<FanMapper, Fan>
        implements FollowService {

    @Autowired
    private ChatTopMapper chatTopMapper;

    @Override
    public Result createFollow(Fan fan) {
        //查询数据
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        Fan tmpFan = lambdaQuery()
                .eq(Fan::getUserId, fan.getUserId())
                .eq(Fan::getFollowUserId, fan.getFollowUserId())
                .eq(Fan::getDeleteFlag, 0)
                .one();
        DynamicDataSourceContextHolder.poll();

        if (Objects.nonNull(tmpFan)) {
            if (tmpFan.getCreateTime().getTime()!=(tmpFan.getUpdateTime().getTime())) {
                return Result.success("only first follow to weidu");
            }

            DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());
            ChatTop tmpTop = new MPJLambdaWrapper<ChatTop>(ChatTop.class)//查询消息页顶部未读
                    .selectAll(ChatTop.class)
                    .eq(ChatTop::getUserId, tmpFan.getFollowUserId())
                    .one();

            //消息页顶部未读赋值
            if (Objects.nonNull(tmpTop)) {
                Integer nowFans = tmpTop.getFanCount() + 1;
                tmpTop.setFanCount(nowFans);
                chatTopMapper.updateById(tmpTop);
            } else {
                tmpTop = new ChatTop();
                tmpTop.setUserId(tmpFan.getFollowUserId());
                tmpTop.setFanCount(1);
                chatTopMapper.insert(tmpTop);
            }

            DynamicDataSourceContextHolder.poll();

            return Result.success();
        } else {
            return Result.failed("db no data");
        }
    }

    @Override
    public Result cancelFollow(Fan fan) {
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());

        Fan tmpFan = lambdaQuery()
                .eq(Fan::getUserId, fan.getUserId())
                .eq(Fan::getFollowUserId, fan.getFollowUserId())
                .eq(Fan::getDeleteFlag, 1)
                .one();

        DynamicDataSourceContextHolder.poll();
        DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());

        if (Objects.nonNull(tmpFan)) {
            ChatTop tmpTop = new MPJLambdaWrapper<ChatTop>(ChatTop.class)//查询消息页顶部未读
                    .selectAll(ChatTop.class)
                    .eq(ChatTop::getUserId, tmpFan.getFollowUserId())
                    .one();

            //消息页顶部未读赋值
            if (Objects.nonNull(tmpTop)) {
                Integer nowFans = tmpTop.getFanCount() - 1;
                tmpTop.setFanCount(nowFans);
                int line = chatTopMapper.updateById(tmpTop);

                DynamicDataSourceContextHolder.poll();

                if (line > 0) {
                    return Result.success();
                } else {
                    return Result.failed("db err");
                }
            } else {
                return Result.failed("db no data");
            }
        } else {
            return Result.failed("db no del");
        }
    }
}