package com.bbs.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.Result;
import com.bbs.chat.dto.param.CancelThumbParam;
import com.bbs.chat.entity.ChatTop;
import com.bbs.chat.entity.Thumb;
import com.bbs.chat.enums.RedisKeys;
import com.bbs.chat.mapper.ChatTopMapper;
import com.bbs.chat.mapper.ThumbMapper;
import com.bbs.chat.service.ThumbService;
import com.bbs.chat.util.RedisUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {
    @Resource
    private RedisUtil redis;

    @Autowired
    private ChatTopMapper chatTopMapper;

    @Autowired
    private ThumbMapper thumbMapper;

    @Override
    public Result createThumb(Thumb thumb) {
        boolean isThumb = redis.hasKey(RedisKeys.NEW_THUMB_COMMENT.key() + thumb.getTcId());

        if (isThumb) {//缓存有数据
            QueryWrapper<Thumb> wrapper = new QueryWrapper();
            wrapper.eq("post_user_id", thumb.getPostUserId())
                    .eq("user_id", thumb.getUserId());
            Thumb tmp = getOne(wrapper);

            if (Objects.nonNull(tmp)) {//多次重复操作点赞按钮
                tmp.setStatus(1);
                updateById(tmp);

                ChatTop top = chatTopMapper.selectById(thumb.getPostUserId());
                if (Objects.nonNull(top)) {
                    top.setAgreeCount(top.getAgreeCount() + 1);
                    chatTopMapper.updateById(top);
                }
            } else {
                thumb.setStatus(1);
                save(thumb);

                ChatTop top = new ChatTop();
                top.setUserId(thumb.getPostUserId());
                top.setAgreeCount(1);
                chatTopMapper.insert(top);
            }

            return Result.success();
        } else {
            return Result.failed("cache no data");
        }
    }

    @Override
    public Result cancelThumb(CancelThumbParam cancelThumbParam) {
        boolean isThumb = redis.hasKey(RedisKeys.NEW_THUMB_COMMENT.key() + cancelThumbParam.getNewId());

        if (isThumb)
            return Result.failed("cache has data");

        switch (cancelThumbParam.getType()) {
            case 1://点赞文章
                QueryWrapper wrap = new QueryWrapper<Thumb>()
                        .eq("tc_id", cancelThumbParam.getNewId())
                        .eq("type", cancelThumbParam.getType())
                        .eq("post_user_id", cancelThumbParam.getPostUserId())
                        .eq("user_id", cancelThumbParam.getUserId());
                Thumb tmp = getOne(wrap);

                tmp.setStatus(0);
                tmp.setUpdateTime(new Date());
                updateById(tmp);
                break;
            case 2://点赞评论
                MPJLambdaWrapper wrap2 = new MPJLambdaWrapper<Thumb>()
                        .selectAll(Thumb.class)
                        .eq(Thumb::getTcId, cancelThumbParam.getCommentId())
                        .eq(Thumb::getType, cancelThumbParam.getType())
                        .eq(Thumb::getPostUserId, cancelThumbParam.getPostUserId())
                        .eq(Thumb::getUserId, cancelThumbParam.getUserId());

                Thumb tmp2 = thumbMapper.selectOne(wrap2);

                tmp2.setStatus(0);
                tmp2.setUpdateTime(new Date());
                updateById(tmp2);
                break;
        }

        ChatTop top = chatTopMapper.selectById(cancelThumbParam.getPostUserId());
        if (Objects.nonNull(top)) {//有消息顶部角标数据
            top.setAgreeCount(top.getAgreeCount() - 1);
            chatTopMapper.updateById(top);
        } else {//无消息顶部角标数据
            return Result.failed("db no data");
        }

        //TODO 取消点赞是否需要再修改未读待定，毕竟消息页上方未读图一乐

        return Result.success();
    }
}