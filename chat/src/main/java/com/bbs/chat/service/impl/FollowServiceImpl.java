package com.bbs.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.chat.entity.ChatTop;
import com.bbs.chat.entity.Fan;
import com.bbs.chat.mapper.ChatTopMapper;
import com.bbs.chat.mapper.FanMapper;
import com.bbs.chat.service.FollowService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

//TODO 2test
@Service
public class FollowServiceImpl extends ServiceImpl<FanMapper, Fan>
        implements FollowService {

    @Autowired
    private ChatTopMapper chatTopMapper;

    @Override
    public Result createFollow(Fan fan) {
        //查询数据
        QueryWrapper<Fan> dbWrap = new QueryWrapper();
        dbWrap
                .eq("user_id", fan.getUserId())
                .eq("follow_user_id", fan.getFollowUserId())
                .eq("delete_flag", 0);
        Fan tmpFan = getOne(dbWrap);

        if (Objects.nonNull(tmpFan)) {
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
            return Result.success();
        } else {
            return Result.failed("db no data");
        }
    }

    @Override
    public Result cancelFollow(Fan fan) {
        QueryWrapper<Fan> dbWrap = new QueryWrapper();
        dbWrap
                .eq("user_id", fan.getUserId())
                .eq("follow_user_id", fan.getFollowUserId())
                .eq("delete_flag", 1);
        Fan tmpFan = getOne(dbWrap);

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