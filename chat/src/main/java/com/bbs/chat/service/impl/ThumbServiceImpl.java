package com.bbs.chat.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.chat.dto.param.CancelThumbParam;
import com.bbs.chat.dto.param.CreateThumbParam;
import com.bbs.chat.entity.ChatTop;
import com.bbs.chat.entity.Thumb;
import com.bbs.chat.enums.DBType;
import com.bbs.chat.enums.RedisKeys;
import com.bbs.chat.mapper.ChatTopMapper;
import com.bbs.chat.mapper.ThumbMapper;
import com.bbs.chat.service.ThumbService;
import com.bbs.chat.util.RedisUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Service
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {
    @Resource
    private RedisUtil redis;

    @Autowired
    private ChatTopMapper chatTopMapper;

    @Override
    public Result createThumb(CreateThumbParam param) {
        if (param.getPostUserId().equals(param.getUserId())) {
            return Result.success("this self thumb");
        }

        Object value = null;
        String key = RedisKeys.NEW_THUMB_COMMENT.key() + param.getNewId();
        switch (param.getType()) {
            case 1://点赞文章
                value = redis.hashGet(key, RedisKeys.NEW_THUMB.key());
                break;
            case 2://点赞评论
                value = redis.hashGet(key, RedisKeys.COMMENT_THUMB.key() + param.getCommentId());
                break;
        }

        if (Objects.nonNull(value)) {//缓存有数据
            //获取点赞对象
            DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
            LambdaQueryChainWrapper<Thumb> wrapper = lambdaQuery()
                    .eq(Thumb::getPostUserId, param.getPostUserId())
                    .eq(Thumb::getUserId, param.getUserId())
                    .eq(Thumb::getType, param.getType());

            switch (param.getType()) {
                case 1://点赞文章
                    wrapper.eq(Thumb::getTcId, param.getNewId());
                    break;
                case 2://点赞评论
                    wrapper.eq(Thumb::getTcId, param.getCommentId());
                    break;
            }
            Thumb tmp = wrapper.one();
            DynamicDataSourceContextHolder.poll();

            if (Objects.nonNull(tmp)) {//重复点赞
                tmp.setStatus(1);
                tmp.setUpdateTime(new Date());

                DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
                updateById(tmp);
                DynamicDataSourceContextHolder.poll();
            } else {//首次点赞
                DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
                save(initThumb(param));
                DynamicDataSourceContextHolder.poll();

                //获取未读实例
                DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());
                MPJLambdaWrapper<ChatTop> wrap = new MPJLambdaWrapper<ChatTop>();
                wrap.selectAll(ChatTop.class)
                        .eq(ChatTop::getUserId, param.getPostUserId());
                ChatTop top = chatTopMapper.selectOne(wrap);

                if (Objects.nonNull(top)) {//未读数量加一
                    top.setAgreeCount(top.getAgreeCount() + 1);
                    chatTopMapper.updateById(top);
                } else {//新建初始化后入库
                    top = new ChatTop();
                    top.setUserId(param.getPostUserId());
                    top.setAgreeCount(1);
                    chatTopMapper.insert(top);
                }

                DynamicDataSourceContextHolder.poll();
            }

            return Result.success();
        } else {
            return Result.failed("cache no data");
        }
    }

    /**
     * 初始化点赞实例
     *
     * @return
     */
    private Thumb initThumb(CreateThumbParam param) {
        Thumb thumb = new Thumb();
        switch (param.getType()) {
            case 1://点赞文章
                thumb.setTcId(param.getNewId());
                break;
            case 2://点赞评论
                thumb.setTcId(param.getCommentId());
                break;
        }
        thumb.setType(param.getType());
        thumb.setPostUserId(param.getPostUserId());
        thumb.setUserId(param.getUserId());

        Date now = new Date();
        thumb.setCreateTime(now);
        thumb.setUpdateTime(now);

        thumb.setTcSummary(param.getTcSummary());
        thumb.setStatus(1);

        return thumb;
    }

    @Override
    public Result cancelThumb(CancelThumbParam param) {
        if (param.getPostUserId().equals(param.getUserId())) {
            return Result.success("this self thumb");
        }

        Object value = null;
        switch (param.getType()) {
            case 1://点赞文章
                value = redis.hashGet(RedisKeys.NEW_THUMB_COMMENT.key() + param.getNewId(), RedisKeys.NEW_THUMB.key());
                break;
            case 2://点赞评论
                value = redis.hashGet(RedisKeys.NEW_THUMB_COMMENT.key() + param.getNewId(), RedisKeys.COMMENT_THUMB.key() + param.getCommentId());
                break;
        }

        if (Objects.nonNull(value))
            return Result.failed("cache has data");

        //取消点赞
        Thumb tmp = getThumb(param);
        if (Objects.isNull(tmp)) {
            return Result.failed("no thumb data");
        }
        tmp.setStatus(0);
        tmp.setUpdateTime(new Date());

        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        updateById(tmp);
        DynamicDataSourceContextHolder.poll();

        //更改未读
        DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());
        ChatTop top = chatTopMapper.selectById(param.getPostUserId());
        if (Objects.nonNull(top)) {//有消息顶部角标数据
            top.setAgreeCount(top.getAgreeCount() - 1);
            chatTopMapper.updateById(top);
        } else {//无消息顶部角标数据
            return Result.failed("db no data");
        }
        DynamicDataSourceContextHolder.poll();

        //TODO 取消点赞是否需要再修改未读待定，毕竟消息页上方未读图一乐

        return Result.success();
    }

    private Thumb getThumb(CancelThumbParam param) {
        Thumb result = null;

        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        LambdaQueryChainWrapper<Thumb> wrapper = lambdaQuery()
                .eq(Thumb::getType, param.getType())
                .eq(Thumb::getPostUserId, param.getPostUserId())
                .eq(Thumb::getUserId, param.getUserId())
                .eq(Thumb::getStatus, 1);

        switch (param.getType()) {
            case 1://点赞文章
                wrapper.eq(Thumb::getTcId, param.getNewId());
                break;
            case 2://点赞评论
                wrapper.eq(Thumb::getTcId, param.getCommentId());
                break;
        }

        result = wrapper.one();
        DynamicDataSourceContextHolder.poll();

        return result;
    }
}