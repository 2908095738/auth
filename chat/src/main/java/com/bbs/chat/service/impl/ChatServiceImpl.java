package com.bbs.chat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.chat.converter.ChatConverter;
import com.bbs.chat.dto.*;
import com.bbs.chat.entity.*;
import com.bbs.chat.mapper.*;
import com.bbs.chat.service.ChatService;
import com.bbs.chat.dto.param.CreateChatParam;
import com.bbs.chat.util.SensitiveFilter;
import com.bbs.chat.util.StringUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl extends MPJBaseServiceImpl<ChatMapper, Chat> implements ChatService {

    @Autowired
    private ChatLastMapper lastMapper;

    @Autowired
    private ThumbMapper thumbMapper;

    @Autowired
    private FanMapper fanMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private ChatTopMapper topMapper;

    private ChatConverter converter;

    private SensitiveFilter sensitiveFilter;

    @Resource
    public void setConverter(ChatConverter converter) {
        this.converter = converter;
    }

    @Resource
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }

    @Override
    public Result createChat(CreateChatParam param, Long userId) {
        Chat chat = converter.toEntity(param);

        //区分消息双方
        if (chat.getSendUid() == -1) {//我发给对方
            chat.setSendUid(userId);
        } else if (chat.getAcceptUid() == -1) {//对方发给我
            chat.setAcceptUid(userId);
        }else{
            return Result.failed("don't confirm who send who");
        }

        //消息内容处理
        String oriContent = chat.getContent();
        oriContent = HtmlUtils.htmlEscape(oriContent);
        String doneContent = sensitiveFilter.filter(oriContent);
        chat.setContent(doneContent);

        save(chat);

        //获取我和对方的最新消息
        MPJLambdaWrapper<ChatLast> lastWrap = new MPJLambdaWrapper(ChatLast.class);
        ChatLast last = lastWrap.selectAll(ChatLast.class)
                .eq(ChatLast::getSendUid, chat.getSendUid())
                .eq(ChatLast::getAcceptUid, chat.getAcceptUid())
                .one();

        boolean isLast = Objects.nonNull(last);
        if (isLast) {//有数据
            last.setCount(last.getCount() + 1);
        } else {//无数据
            last = new ChatLast();
            last.setSendUid(chat.getSendUid());
            last.setAcceptUid(chat.getAcceptUid());
            last.setCount(1);
        }
        last.setContentType(chat.getContentType());

        //最新消息过长裁剪
        if (last.getContentType() == 1) {
            String tmpContent = chat.getContent();
            tmpContent = StringUtil.abbreviate(tmpContent, 16);
            last.setContentLast(tmpContent);
        }

        last.setTimeLast(chat.getTime());

        if (isLast) {
            lastMapper.updateById(last);
        } else {
            lastMapper.insert(last);
        }

        //TODO 发通知提醒用户查看未读消息

        return Result.success();
    }

    @Override
    public ChatTopDto getChatTop(Long userId) {
        MPJLambdaWrapper wrapper = new MPJLambdaWrapper<ChatTopDto>()
                .select(ChatTop::getAgreeCount, ChatTop::getFanCount, ChatTop::getCommentCount)
                .eq(ChatTop::getUserId, userId);

        return topMapper.selectJoinOne(ChatTopDto.class, wrapper);
    }

    @Override
    public ChatListDto getChat(Long userId, Integer current, Integer size) {

        /**
         * TODO 前几个消息(头牌消息)需要存入redis，优先保证企业用户看到。
         *  例如其他用户给我发消息、用户自己删除消息就立即调整redis中的头牌消息
         *  -
         *      返回两个列表，一个是未读置顶，另一个是已读列表，两列表都是时间倒叙
         *  -
         *      长时间未登录，将其相关数据全部转入低速持久化
         *  -
         *      未读消息日期修正，例如一月内修正成多少天前的消息等
         */

        MPJLambdaWrapper wrapper = new MPJLambdaWrapper<ChatListDto.ChatLastDto>()
                .select(ChatLast::getId)
                .selectAs(ChatLast::getSendUid, ChatListDto.ChatLastDto::getSendUid)
                .selectAs(UserAccount::getNickName, ChatListDto.ChatLastDto::getSendName)
                .selectAs(UserAccount::getAvatarPath, ChatListDto.ChatLastDto::getAvatarPath)
                .select(ChatLast::getCount)
                .selectAs(ChatLast::getContentType, ChatListDto.ChatLastDto::getContentType)
                .selectAs(ChatLast::getContentLast, ChatListDto.ChatLastDto::getContentLast)
                .selectAs(ChatLast::getTimeLast, ChatListDto.ChatLastDto::getTimeLast)

                .leftJoin(UserAccount.class, UserAccount::getUserId, ChatLast::getSendUid)
                .eq(ChatLast::getAcceptUid, userId)
                .orderBy(true, false, ChatLast::getTimeLast);

        Page page = lastMapper.selectJoinMapsPage(new Page(current, size), wrapper);

        ChatListDto result = new ChatListDto();
        result.setLastList(page);
        return result;
    }

    @Override
    public Page<ChatRecordDto> getRecord(Long sendUid, Long acceptUid, Integer current, Integer size) {
        /**
         * TODO 仿照微信聊天记录有相应时间段,聊天记录存放用户本地
         *  -
         *      复杂SQL难搞暂时用着(getComm方法同理)
         */

        //我发给别人的消息列表
        MPJLambdaWrapper acceptWrap = new MPJLambdaWrapper<ChatRecordDto>()
                .select(Chat::getId)
                .select(Chat::getContentType, Chat::getContent, Chat::getTime)

                .eq(Chat::getSendUid, sendUid)
                .eq(Chat::getAcceptUid, acceptUid)
                .eq(Chat::getStatus, 0)
                .orderBy(true, false, Chat::getTime);

        Page<ChatRecordDto> page = selectJoinListPage(new Page(current, size / 2), ChatRecordDto.class, acceptWrap);

        //清除消息页对应消息角标
        UpdateJoinWrapper<ChatLast> updWrap = JoinWrappers.update(ChatLast.class)
                .set(ChatLast::getCount, 0)
                .eq(ChatLast::getSendUid, sendUid)
                .eq(ChatLast::getAcceptUid, acceptUid);
        lastMapper.update(updWrap);

        //正常情况下评论、回复各生成一半，评论数量不足其余全由回复补上
        long total = page.getTotal();
        long little = size - total;

        //别人发给我的消息列表
        MPJLambdaWrapper sendWrap = new MPJLambdaWrapper<ChatRecordDto>()
                .select(Chat::getId)
                .selectAs(Chat::getSendUid, ChatRecordDto::getChatUid)
                .select(Chat::getContentType, Chat::getContent, Chat::getTime)

                .eq(Chat::getSendUid, acceptUid)
                .eq(Chat::getAcceptUid, sendUid)
                .eq(Chat::getStatus, 0)
                .orderBy(true, false, Chat::getTime);

        List<ChatRecordDto> sendList = selectJoinListPage(new Page(current, little), ChatRecordDto.class, sendWrap).getRecords();

        //两张表查出来的数据合并
        // boolean isNonNull_2 = Objects.nonNull(sendList.get(0));//查不出来数据，但List却有一个元素，但该元素又是空
        if (!sendList.isEmpty() /*&& isNonNull_2*/) {
            if (page.getRecords().isEmpty()) {
                page.setRecords(new ArrayList());
            } else {
                page.getRecords().forEach(c -> c.setChatUid(-1L));
            }
            page.getRecords().addAll(sendList);
            page.getRecords().sort((l, r) -> {
                Date lTime = l.getTime();
                Date rTime = r.getTime();

                return rTime.compareTo(lTime);
            });
        }
        page.setTotal(page.getRecords().size());

        return page;
    }

    @Override
    public Page<AgreeDto> getAgree(Long userId, Integer current, Integer size) {

        //TODO 因为是多张表，所以唯一标识符会冲突，但暂时复杂SQL不会，故将算力交给终端设备解决

        //查询点赞列表
        MPJLambdaWrapper thumbWrap = new MPJLambdaWrapper<AgreeDto>()
                .selectAs(Thumb::getTcId, AgreeDto::getAgreeId)
                .selectAs(UserAccount::getUserId, AgreeDto::getAgreeUid)
                .selectAs(UserAccount::getNickName, AgreeDto::getNickName)
                .selectAs(UserAccount::getAvatarPath, AgreeDto::getAvatarPath)
                .selectAs(Thumb::getType, AgreeDto::getType)
                .selectAs(Thumb::getCreateTime, AgreeDto::getTime)

                .rightJoin(UserAccount.class, UserAccount::getUserId, Thumb::getUserId)
                .ne(Thumb::getPostUserId, Thumb::getUserId)
                .eq(Thumb::getPostUserId, userId)
                .orderBy(true, false, Thumb::getUpdateTime);

        Page<AgreeDto> page = thumbMapper.selectJoinPage(new Page(current, size / 2), AgreeDto.class, thumbWrap);

        //查询收藏列表
        long total = page.getTotal();
        long little = size - total;

        MPJLambdaWrapper facWrap = new MPJLambdaWrapper<AgreeDto>()
                .selectAs(Favorites::getNewId, AgreeDto::getAgreeId)
                .selectAs(UserAccount::getUserId, AgreeDto::getAgreeUid)
                .selectAs(UserAccount::getNickName, AgreeDto::getNickName)
                .selectAs(UserAccount::getAvatarPath, AgreeDto::getAvatarPath)
                .selectAs(Favorites::getCreateTime, AgreeDto::getTime)

                .leftJoin(Favorites.class, Favorites::getNewId, News::getNewId)
                .leftJoin(UserAccount.class, UserAccount::getUserId, Favorites::getUserId)
                .eq(News::getCreateId, userId)
                .orderBy(true, false, Thumb::getUpdateTime);

        List<AgreeDto> tmp = newsMapper.selectJoinPage(new Page(current, little), AgreeDto.class, facWrap).getRecords();

        //两张表查出来的数据合并
//        boolean isNonNull_2 = Objects.nonNull(tmp.get(0));//查不出来数据，但List却有一个元素，但该元素又是空
        if (!tmp.isEmpty() /*&& isNonNull_2*/) {
            tmp.forEach(a -> a.setType(3));
            if (page.getRecords().isEmpty()) {
                page.setRecords(new ArrayList());
            }
            page.getRecords().addAll(tmp);
            page.getRecords().sort((l, r) -> {
                Date lTime = l.getTime();
                Date rTime = r.getTime();

                return rTime.compareTo(lTime);
            });
        }
        page.setTotal(page.getRecords().size());

        //清除点赞、收藏未读角标
        ChatTop top = topMapper.selectById(userId);
        top.setAgreeCount(0);
        topMapper.updateById(top);

        return page;
    }

    @Override
    public Page<FanDto> getFan(Long userId, Integer current, Integer size) {
        MPJLambdaWrapper wrapper = new MPJLambdaWrapper<FanDto>()
                .selectAs(UserAccount::getUserId, FanDto::getFanUid)
                .selectAs(UserAccount::getNickName, FanDto::getNickName)
                .selectAs(UserAccount::getAvatarPath, FanDto::getAvatarPath)
                .select(Fan::getType)
                .selectAs(Fan::getCreateTime, FanDto::getTime)
                .leftJoin(UserAccount.class, UserAccount::getUserId, Fan::getUserId)
                .eq(Fan::getFollowUserId, userId)
                .eq(Fan::getDeleteFlag, 0)
                .in(Fan::getType, 0, 3)
                .orderBy(true, false, Fan::getCreateTime);

        Page page = fanMapper.selectJoinMapsPage(new Page<>(current, size), wrapper);
        return page;
    }

    @Override
    public Page<CommDto> getComm(Long userId, Integer current, Integer size) {
        //评论文章
        MPJLambdaWrapper newWrap = new MPJLambdaWrapper<CommDto>()
                .selectAs(Comment::getCreateId, CommDto::getCommUid)
                .selectAs(UserAccount::getNickName, CommDto::getNickName)
                .selectAs(UserAccount::getAvatarPath, CommDto::getAvatarPath)
                .selectAs(Comment::getId, CommDto::getCommId)
                .selectAs(Comment::getUpdateTime, CommDto::getTime)

                .leftJoin(Comment.class, Comment::getNewId, News::getNewId)
                .leftJoin(UserAccount.class, UserAccount::getUserId, Comment::getCreateId)
                .isNull("parent_id")
                .eq(News::getCreateId, userId)
                .eq(Comment::getStatus, 20)
                .orderBy(true, false, Comment::getUpdateTime);

        Page<CommDto> page = newsMapper.selectJoinPage(new Page(current, size / 2), CommDto.class, newWrap);
        page.getRecords().forEach(d -> d.setType(1));//TODO 没找到MyBatisPlus赋默认值的方法

        //正常情况下评论、回复各生成一半，评论数量不足其余全由回复补上
        long total = page.getTotal();
        long little = size - total;

        //回复评论
        MPJLambdaWrapper<Comment> pidWrap = new MPJLambdaWrapper<Comment>()//被回复评论id
                .select(Comment::getId)
                .eq(Comment::getCreateId, userId)
                .eq(Comment::getStatus, 20);

        List<Long> ids = commentMapper.selectList(pidWrap)
                .stream().map(c -> c.getId()).collect(Collectors.toList());

        if (!ids.isEmpty()) {
            MPJLambdaWrapper commWrap = new MPJLambdaWrapper<CommDto>()
                    .selectAs(Comment::getCreateId, CommDto::getCommUid)
                    .selectAs(UserAccount::getNickName, CommDto::getNickName)
                    .selectAs(UserAccount::getAvatarPath, CommDto::getAvatarPath)
                    .selectAs(Comment::getId, CommDto::getCommId)
                    .selectAs(Comment::getUpdateTime, CommDto::getTime)

                    .leftJoin(UserAccount.class, UserAccount::getUserId, Comment::getCreateId)
                    .in(true, "parent_id", ids)
                    .eq(Comment::getStatus, 20)
                    .orderBy(true, false, Comment::getUpdateTime);

            List<CommDto> tmp = commentMapper.selectJoinPage(new Page(current, little), CommDto.class, commWrap).getRecords();

            //两张表查出来的数据合并
            //boolean isNonNull_2 = Objects.nonNull(tmp.get(0));//查不出来数据，但List却有一个元素，但该元素又是空
            if (!tmp.isEmpty() /*&& isNonNull_2*/) {
                tmp.forEach(dto -> dto.setType(2));
                if (page.getRecords().isEmpty()) {
                    page.setRecords(new ArrayList());
                }
                page.getRecords().addAll(tmp);
                page.getRecords().sort((l, r) -> {
                    Date lTime = l.getTime();
                    Date rTime = r.getTime();

                    return rTime.compareTo(lTime);
                });
            }
            page.setTotal(page.getRecords().size());
        }

        return page;
    }

    @Override
    public Page<String> getNick(Long userId, Integer current, Integer size) {
        MPJLambdaWrapper wrapper = new MPJLambdaWrapper<String>()
                .select(UserAccount::getNickName)
                .leftJoin(UserAccount.class, UserAccount::getUserId, Fan::getFollowUserId)
                .eq(Fan::getUserId, userId)
                .eq(Fan::getDeleteFlag, 0);

        return fanMapper.selectJoinPage(new Page(current, size), String.class, wrapper);
    }

    @Override
    public Result toFan(Long sendUid, Long acceptUid, Integer type) {
        switch (type) {
            case 1://互相关注
                UpdateJoinWrapper<Fan> toFanwrap = JoinWrappers.update(Fan.class)
                        .set(Fan::getType, 3)
                        .set(Fan::getUpdateTime, new Date())
                        .eq(Fan::getUserId, sendUid)
                        .eq(Fan::getFollowUserId, acceptUid);

                int line = fanMapper.update(toFanwrap);
                if (line == 0) {
                    return Result.failed("hu fen fail");
                }

                Fan tmp = new Fan();
                tmp.setUserId(acceptUid);
                tmp.setFollowUserId(sendUid);
                tmp.setType(3);
                tmp.setCreateTime(new Date());
                tmp.setUpdateTime(new Date());

                line = fanMapper.insert(tmp);
                if (line == 0) {
                    return Result.failed("hu fen fail");
                } else {
                    return Result.success();
                }

            case 2://取消互相关注
                UpdateJoinWrapper<Fan> wrapper = JoinWrappers.update(Fan.class)
                        .set(Fan::getType, 0)
                        .set(Fan::getUpdateTime, new Date())
                        .eq(Fan::getUserId, sendUid)
                        .eq(Fan::getFollowUserId, acceptUid);

                int line_2 = fanMapper.update(wrapper);
                if (line_2 == 0) {
                    return Result.failed("hu fen fail");
                }

                Map<String, Object> delColEq = new HashMap();
                delColEq.put("account_id", acceptUid);
                delColEq.put("f_account_id", sendUid);
                line_2 = fanMapper.deleteByMap(delColEq);
                if (line_2 > 0) {
                    return Result.success();
                } else {
                    return Result.failed("quxiao hu fen fail");
                }
        }

        return Result.success();
    }

    @Override
    public void delChat(List<Long> chatIds, Integer type) {
        switch (type) {
            case 1:
                lastMapper.deleteBatchIds(chatIds);
                break;
            case 2:
                Map<String, Long> idMap = chatIds.stream().collect(Collectors.toMap(k -> "id", v -> v));

                update()
                        .set("status", 1)
                        .allEq(idMap)
                        .update();
                break;
        }
    }
}