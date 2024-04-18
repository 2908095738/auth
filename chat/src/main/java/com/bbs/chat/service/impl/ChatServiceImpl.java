package com.bbs.chat.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.Auth;
import com.bbs.chat.bo.UserBO;
import com.bbs.chat.converter.ChatConverter;
import com.bbs.chat.dto.*;
import com.bbs.chat.entity.*;
import com.bbs.chat.enums.DBType;
import com.bbs.chat.mapper.*;
import com.bbs.chat.service.ChatService;
import com.bbs.chat.dto.param.CreateChatParam;
import com.bbs.chat.util.LambdaUtil;
import com.bbs.chat.util.SensitiveFilter;
import com.bbs.chat.util.StringUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.toolkit.LambdaUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
public class ChatServiceImpl extends MPJBaseServiceImpl<ChatMapper, Chat> implements ChatService {

    @Resource
    private ChatLastMapper lastMapper;

    @Resource
    private ThumbMapper thumbMapper;

    @Resource
    private FanMapper fanMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private NewsMapper newsMapper;

    @Resource
    private ChatTopMapper topMapper;

    @Resource
    private UserMapper userMapper;

    private ChatConverter converter;

    private SensitiveFilter sensitiveFilter;

    @Resource
    private Auth.UserAPI api;

    @Resource
    public void setConverter(ChatConverter converter) {
        this.converter = converter;
    }

    @Resource
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }

    @DS("chat")
    @Override
    public Result<ChatRecordDto> createChat(CreateChatParam param, Long userId) {
        Chat chat = converter.toEntity(param);
        chat.setSendUid(userId);
        chat.setTime(new Date());

        //消息内容处理
        String oriContent = chat.getContent();
        oriContent = HtmlUtils.htmlEscape(oriContent);
        String doneContent = sensitiveFilter.filter(oriContent);
        chat.setContent(doneContent);

        save(chat);

        //对方更新消息
        MPJLambdaWrapper<ChatLast> lastWrap = new MPJLambdaWrapper<>(ChatLast.class);
        ChatLast last = lastWrap.selectAll(ChatLast.class)
                .eq(ChatLast::getSendUid, chat.getSendUid())
                .eq(ChatLast::getAcceptUid, chat.getAcceptUid())
                .one();

        boolean isLast = nonNull(last);
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

        //我更新消息
        MPJLambdaWrapper<ChatLast> lastMeWrap = new MPJLambdaWrapper<>(ChatLast.class);
        ChatLast lastMe = lastMeWrap.selectAll(ChatLast.class)
                .eq(ChatLast::getSendUid, chat.getAcceptUid())
                .eq(ChatLast::getAcceptUid, chat.getSendUid())
                .one();

        boolean isLastMe = nonNull(lastMe);
        if (!isLastMe) {//无数据
            lastMe = new ChatLast();
            lastMe.setSendUid(chat.getAcceptUid());
            lastMe.setAcceptUid(chat.getSendUid());
            lastMe.setCount(0);
        }
        lastMe.setContentType(chat.getContentType());

        //最新消息过长裁剪
        if (lastMe.getContentType() == 1) {
            String tmpContent = chat.getContent();
            tmpContent = StringUtil.abbreviate(tmpContent, 16);
            lastMe.setContentLast(tmpContent);
        }

        lastMe.setTimeLast(chat.getTime());

        if (isLastMe) {
            lastMapper.updateById(lastMe);
        } else {
            lastMapper.insert(lastMe);
        }

        //TODO 发通知提醒用户查看未读消息

        Auth.UserAPI.User loginUser = api.getLoginUser();
        ChatRecordDto dto = converter.toDTO(chat);
        dto.setChatUid(loginUser.getId());
        dto.setAvatar(loginUser.getAvatar());
        dto.setType(NumberUtils.INTEGER_ONE);
        return Result.success(dto);
    }

    @DS("chat")
    @Override
    public ChatTopDto getChatTop(Long userId) {
        MPJLambdaWrapper wrapper = new MPJLambdaWrapper<ChatTopDto>()
                .select(ChatTop::getAgreeCount, ChatTop::getFanCount, ChatTop::getCommentCount)
                .eq(ChatTop::getUserId, userId);

        return topMapper.selectJoinOne(ChatTopDto.class, wrapper);
    }

    @Override
    public Result<ChatListDto> getChat(Long userId, Integer current, Integer size) {

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

        DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());
        MPJLambdaWrapper<ChatLast> wrap = new MPJLambdaWrapper<>(ChatLast.class);
        wrap.select(ChatLast::getSendUid)
                .eq(ChatLast::getAcceptUid, userId);

        List<Long> userIds = lastMapper.selectJoinList(Long.class, wrap);
        if (Objects.isNull(userIds) || userIds.isEmpty()) {
            return Result.success(null);
        }

        // 查询用户信息
        Map<Long, Auth.UserAPI.User> tmpMap = api.getUserList(userIds).stream().collect(Collectors.toMap(Auth.UserAPI.User::getId, item -> item));

        MPJLambdaWrapper<ChatListDto.ChatLastDto> wrapper = new MPJLambdaWrapper(ChatLast.class);
        Page<ChatListDto.ChatLastDto> page = wrapper
                .select(ChatLast::getId)
                .selectAs(ChatLast::getSendUid, ChatListDto.ChatLastDto::getSendUid)
                .select(ChatLast::getCount)
                .selectAs(ChatLast::getContentType, ChatListDto.ChatLastDto::getContentType)
                .selectAs(ChatLast::getContentLast, ChatListDto.ChatLastDto::getContentLast)
                .selectAs(ChatLast::getTimeLast, ChatListDto.ChatLastDto::getTimeLast)

                .eq(ChatLast::getAcceptUid, userId)
                .orderByDesc(ChatLast::getTimeLast)
                .page(new Page(current, size), ChatListDto.ChatLastDto.class);

        DynamicDataSourceContextHolder.poll();
        page.getRecords().forEach(l -> {
            Auth.UserAPI.User user = tmpMap.get(l.getSendUid());
            l.setSendName(user.getName());
            l.setAvatarPath(user.getAvatar());
            l.setTimeLastStr(DateUtil.format(l.getTimeLast(), "yyyy-MM-dd"));
        });

        ChatListDto result = new ChatListDto();
        result.setLastList(page);
        return Result.success(result);
    }

    @DS("chat")
    @Override
    public Page<ChatRecordDto> getRecord(Long targetUID, Integer current, Integer size) {
        Auth.UserAPI.User loginUser = api.getLoginUser();
        Long loginUserId = loginUser.getId();
        Page<Chat> chats = lambdaQuery()
                .eq(Chat::getStatus, NumberUtils.INTEGER_ZERO)
                .nested(wrapper -> wrapper
                        .eq(Chat::getSendUid, loginUserId)
                        .eq(Chat::getAcceptUid, targetUID)
                )
                .or(wrapper -> wrapper
                        .eq(Chat::getSendUid, targetUID)
                        .eq(Chat::getAcceptUid, loginUserId)
                )
                .page(new Page<>(current, size));

        List<Chat> records = chats.getRecords();
        int chatNumber = records.size();
        List<ChatRecordDto> dtoList = new ArrayList<>(chatNumber);
        List<ChatRecordDto> needFillAvatarDTOList = new ArrayList<>();
        Set<Long> needFillAvatarChatUIDs = new HashSet<>();
        for (Chat chat : records) {
            ChatRecordDto dto = converter.toDTO(chat);
            if (chat.getSendUid().equals(loginUserId)) {
                // 发给别人的消息
                dto.setType(NumberUtils.INTEGER_ONE);
                dto.setAvatar(loginUser.getAvatar());
                dto.setChatUid(loginUserId);
            } else {
                // 收到的消息
                dto.setType(NumberUtils.INTEGER_ZERO);
                dto.setChatUid(chat.getSendUid());
                needFillAvatarChatUIDs.add(dto.getChatUid());
                needFillAvatarDTOList.add(dto);
            }
            dtoList.add(dto);
        }
        if(needFillAvatarDTOList.size() > NumberUtils.INTEGER_ZERO) {
            List<Auth.UserAPI.User> userList = api.getUserList(new ArrayList<>(needFillAvatarChatUIDs));
            if(nonNull(userList) && userList.size() > NumberUtils.INTEGER_ZERO) {
                Map<Long, Auth.UserAPI.User> idAndUserMap = userList.stream().collect(Collectors.toMap(Auth.UserAPI.User::getId, user -> user));
                for (int index = NumberUtils.INTEGER_ZERO; index < needFillAvatarDTOList.size(); index++) {
                    ChatRecordDto dto = needFillAvatarDTOList.get(index);
                    Auth.UserAPI.User user = idAndUserMap.get(dto.getChatUid());
                    if(nonNull(user)) dto.setAvatar(user.getAvatar());
                }
            }
        }
        Page<ChatRecordDto> result = new Page<>(chats.getCurrent(), chats.getSize(), chats.getTotal());
        result.setRecords(dtoList);
        return result;
    }

    @Override
    public Page<AgreeDto> getAgree(Long userId, Integer current, Integer size) {

        //TODO 因为是多张表，所以唯一标识符会冲突，但暂时复杂SQL不会，故将算力交给终端设备解决

        //获取点赞用户id列表
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper<Thumb> userIdWrap = new MPJLambdaWrapper<Thumb>()
                .select(Thumb::getUserId)
                .ne(Thumb::getPostUserId, Thumb::getUserId)
                .eq(Thumb::getPostUserId, userId);

        List<Long> userIds = thumbMapper.selectJoinList(Long.class, userIdWrap);
        DynamicDataSourceContextHolder.poll();

        //获取点赞用户信息列表
        Page<AgreeDto> page = null;
        DynamicDataSourceContextHolder.push(DBType.AUTH.getDbName());
        MPJLambdaWrapper<User> userWrap = new MPJLambdaWrapper<User>()
                .select(User::getId, User::getName)
                .in(User::getId, userIds);

        boolean isThumbUserIds = Objects.nonNull(userIds) && !userIds.isEmpty();
        if (isThumbUserIds) {
            Map<Long, List<UserBO>> userMap = userMapper.selectJoinList(UserBO.class, userWrap)
                    .stream()
                    .collect(Collectors.groupingBy(UserBO::getId));
            DynamicDataSourceContextHolder.poll();

            //查询点赞列表
            DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
            MPJLambdaWrapper thumbWrap = new MPJLambdaWrapper<AgreeDto>()
                    .selectAs(Thumb::getTcId, AgreeDto::getAgreeId)
                    .selectAs(Thumb::getUserId, AgreeDto::getAgreeUid)
                    .selectAs(Thumb::getType, AgreeDto::getType)
                    .selectAs(Thumb::getCreateTime, AgreeDto::getTime)

                    .ne(Thumb::getPostUserId, Thumb::getUserId)
                    .eq(Thumb::getPostUserId, userId)
                    .orderBy(true, false, Thumb::getUpdateTime);

            page = thumbMapper.selectJoinPage(new Page(current, size / 2), AgreeDto.class, thumbWrap);
            DynamicDataSourceContextHolder.poll();

            //点赞列表补值
            page.getRecords().forEach(t -> {
                List<UserBO> bos = userMap.get(t.getAgreeUid());
                UserBO tmpUser = bos.get(0);
                t.setName(tmpUser.getName());
            });
        }

        //获取收藏待查询条数
        long little;
        if (Objects.nonNull(page)) {
            long total = page.getSize();
            little = size - total;
        } else {
            little = size;
        }


        //获取收藏用户id列表
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper userIdByFavo = new MPJLambdaWrapper<News>()
                .select(Favorites::getUserId)
                .leftJoin(Favorites.class, Favorites::getNewId, News::getNewId)
                .ne(Favorites::getUserId, userId)
                .eq(News::getCreateId, userId);

        List<Long> userIdsByFavo = newsMapper.selectJoinList(Long.class, userIdByFavo);
        DynamicDataSourceContextHolder.poll();

        boolean isFavoUserIds = Objects.nonNull(userIdsByFavo) && !userIdsByFavo.isEmpty();
        if (!isThumbUserIds && !isFavoUserIds) {
            return new Page();
        }

        //获取收藏用户信息列表
        DynamicDataSourceContextHolder.push(DBType.AUTH.getDbName());
        MPJLambdaWrapper<User> userWrapByFavo = new MPJLambdaWrapper<User>()
                .select(User::getId, User::getName)
                .in(User::getId, userIdsByFavo);

        Map<Long, List<UserBO>> userByFavoMap = userMapper.selectJoinList(UserBO.class, userWrapByFavo)
                .stream()
                .collect(Collectors.groupingBy(UserBO::getId));
        DynamicDataSourceContextHolder.poll();

        //查询收藏列表
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper facWrap = new MPJLambdaWrapper<AgreeDto>()
                .selectAs(Favorites::getNewId, AgreeDto::getAgreeId)
                .selectAs(Favorites::getUserId, AgreeDto::getAgreeUid)
                .selectAs(Favorites::getCreateTime, AgreeDto::getTime)

                .rightJoin(Favorites.class, Favorites::getNewId, News::getNewId)
                .ne(Favorites::getUserId, userId)
                .eq(News::getCreateId, userId)
                .orderBy(true, false, Thumb::getUpdateTime);

        List<AgreeDto> tmp = newsMapper.selectJoinPage(new Page(current, little), AgreeDto.class, facWrap).getRecords();
        DynamicDataSourceContextHolder.poll();

        //收藏列表补值
        tmp.forEach(t -> {
            UserBO tmpUser = userByFavoMap.get(t.getAgreeUid()).get(0);
            t.setName(tmpUser.getName());
        });

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

        DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());

        //清除点赞、收藏未读角标
        ChatTop top = topMapper.selectById(userId);
        top.setAgreeCount(0);
        topMapper.updateById(top);

        DynamicDataSourceContextHolder.poll();

        //获取用户头像
        List<Long> userIdTMP = new ArrayList();
        if (Objects.nonNull(userIds) && !userIds.isEmpty()) {
            userIdTMP.addAll(userIds);
        }

        if (Objects.nonNull(userIdsByFavo) && userIdsByFavo.isEmpty()) {
            userIdTMP.addAll(userIdsByFavo);
        }

        if (!userIdTMP.isEmpty()) {
            Map<Long, String> avatarMap = api.getUserList(userIdTMP)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(LambdaUtil.distinct(Auth.UserAPI.User::getId))
                    .collect(
                            Collectors.toMap(Auth.UserAPI.User::getId, u -> u.getAvatar()));

            page.getRecords().forEach(dto -> {
                String avatar = avatarMap.get(dto.getAgreeUid());
                dto.setAvatarPath(avatar);
            });
        }

        return page;
    }

    @Override
    public Page<FanDto> getFan(Long userId, Integer current, Integer size) {
        //获取新增关注用户id
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper userIdWrap = new MPJLambdaWrapper<Fan>()
                .select(Fan::getUserId)
                .eq(Fan::getFollowUserId, userId)
                .in(Fan::getType, 0, 3);

        List<Long> userIds = fanMapper.selectJoinList(Long.class, userIdWrap);
        DynamicDataSourceContextHolder.poll();

        if (Objects.isNull(userIds) || userIds.isEmpty()) {
            return new Page();
        }

        //获取新增关注用户信息列表
        DynamicDataSourceContextHolder.push(DBType.AUTH.getDbName());
        MPJLambdaWrapper<User> userWrapByFavo = new MPJLambdaWrapper<User>()
                .select(User::getId, User::getName)
                .in(User::getId, userIds);

        Map<Long, List<UserBO>> userByFanMap = userMapper.selectJoinList(UserBO.class, userWrapByFavo)
                .stream()
                .collect(Collectors.groupingBy(UserBO::getId));
        DynamicDataSourceContextHolder.poll();

        //获取新增关注列表
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper wrapper = new MPJLambdaWrapper<FanDto>()
                .select(Fan::getType)
                .selectAs(Fan::getCreateTime, FanDto::getTime)
                .selectAs(Fan::getUserId, FanDto::getFanUid)
                .eq(Fan::getFollowUserId, userId)
                .in(Fan::getType, 0, 3)
                .orderBy(true, false, Fan::getCreateTime);

        Page<FanDto> page = fanMapper.selectJoinPage(new Page(current, size), FanDto.class, wrapper);
        DynamicDataSourceContextHolder.poll();

        //新增关注列表补值
        page.getRecords().forEach(t -> {
            UserBO tmpUser = userByFanMap.get(t.getFanUid()).get(0);
            t.setName(tmpUser.getName());
        });

        //清除新增关注未读角标
        DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());
        ChatTop top = topMapper.selectById(userId);
        top.setFanCount(0);
        topMapper.updateById(top);
        DynamicDataSourceContextHolder.poll();
        return page;
    }

    @Override
    public Page<CommDto> getComm(Long userId, Integer current, Integer size) {
        //获取评论文章用户id列表
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper userIdWrap = new MPJLambdaWrapper<News>()
                .select(Comment::getCreateId)
                .leftJoin(Comment.class, Comment::getNewId, News::getNewId)
                .isNull("parent_id")
                .eq(News::getCreateId, userId)
                .eq(Comment::getStatus, 20);

        List<Long> userIds = newsMapper.selectJoinList(Long.class, userIdWrap);
        DynamicDataSourceContextHolder.poll();

        //获取评论文章用户信息列表
        Page<CommDto> page = null;
        DynamicDataSourceContextHolder.push(DBType.AUTH.getDbName());
        MPJLambdaWrapper<User> userWrapByNew = new MPJLambdaWrapper<User>()
                .select(User::getId, User::getName)
                .in(User::getId, userIds);

        boolean isNewUserIds = Objects.nonNull(userIds) && !userIds.isEmpty();
        if (isNewUserIds) {
            Map<Long, List<UserBO>> userByNewMap = userMapper.selectJoinList(UserBO.class, userWrapByNew)
                    .stream()
                    .collect(Collectors.groupingBy(UserBO::getId));
            DynamicDataSourceContextHolder.poll();

            //获取评论文章信息分页
            DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
            MPJLambdaWrapper newWrap = new MPJLambdaWrapper<CommDto>()
                    .selectAs(Comment::getCreateId, CommDto::getCommUid)
                    .selectAs(Comment::getId, CommDto::getCommId)
                    .selectAs(Comment::getUpdateTime, CommDto::getTime)

                    .leftJoin(Comment.class, Comment::getNewId, News::getNewId)
                    .ne(Comment::getCreateId, userId)
                    .isNull(Comment::getParentId)
                    .eq(News::getCreateId, userId)
                    .eq(Comment::getStatus, 20)
                    .orderBy(true, false, Comment::getUpdateTime);

            page = newsMapper.selectJoinPage(new Page(current, size / 2), CommDto.class, newWrap);
            DynamicDataSourceContextHolder.poll();
            page.getRecords().forEach(d -> {
                d.setType(1);

                UserBO tmpUser = userByNewMap.get(d.getCommUid()).get(0);
                d.setName(tmpUser.getName());
            });
        }

        //正常情况下评论、回复各生成一半，评论数量不足其余全由回复补上
        long little;
        if (Objects.nonNull(page)) {
            long total = page.getTotal();
            little = size - total;
        } else {
            little = size;
        }

        //获取回复评论id列表
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper<Comment> pidWrap = new MPJLambdaWrapper<Comment>()//被回复评论id
                .select(Comment::getId)
                .eq(Comment::getDeleteFlag, 0)
                .eq(Comment::getCreateId, userId)
                .eq(Comment::getStatus, 20);

        List<Long> ids = commentMapper.selectList(pidWrap)
                .stream().map(c -> c.getId()).collect(Collectors.toList());

        boolean isCommUserIds = Objects.nonNull(ids) && !ids.isEmpty();
        if (!isNewUserIds && !isCommUserIds) {
            return new Page();
        }

        //获取回复评论用户id列表
        MPJLambdaWrapper<Comment> userIdWrapByComm = new MPJLambdaWrapper<Comment>()
                .select(Comment::getCreateId)
                .in(Comment::getParentId, ids)
                .eq(Comment::getStatus, 20);

        List<Long> userIdsByComm = commentMapper.selectJoinList(Long.class, userIdWrapByComm);
        DynamicDataSourceContextHolder.poll();

        if (Objects.isNull(userIdsByComm) || userIdsByComm.isEmpty()) {
            //清除评论未读角标
            DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());
            ChatTop top = topMapper.selectById(userId);
            top.setCommentCount(0);
            topMapper.updateById(top);
            DynamicDataSourceContextHolder.poll();

            return page;//TODO 没人回复我的评论，暂时直接返回，没有考虑补足数量
        }

        //获取回复评论用户列表
        DynamicDataSourceContextHolder.push(DBType.AUTH.getDbName());
        MPJLambdaWrapper<User> wrapByComm = new MPJLambdaWrapper<User>()
                .select(User::getId, User::getName)
                .in(User::getId, userIdsByComm);

        Map<Long, List<UserBO>> userMapByComm = userMapper.selectJoinList(UserBO.class, wrapByComm)
                .stream()
                .collect(Collectors.groupingBy(UserBO::getId));
        DynamicDataSourceContextHolder.poll();

        //获取回复评论列表
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        if (!ids.isEmpty()) {
            MPJLambdaWrapper commWrap = new MPJLambdaWrapper<CommDto>()
                    .selectAs(Comment::getCreateId, CommDto::getCommUid)
                    .selectAs(Comment::getId, CommDto::getCommId)
                    .selectAs(Comment::getUpdateTime, CommDto::getTime)

                    .in(Comment::getParentId, ids)
                    .eq(Comment::getStatus, 20)
                    .orderBy(true, false, Comment::getUpdateTime);

            List<CommDto> tmp = commentMapper.selectJoinPage(new Page(current, little), CommDto.class, commWrap).getRecords();
            DynamicDataSourceContextHolder.poll();

            //两张表查出来的数据合并
            //boolean isNonNull_2 = Objects.nonNull(tmp.get(0));//查不出来数据，但List却有一个元素，但该元素又是空
            if (!tmp.isEmpty() /*&& isNonNull_2*/) {
                tmp.forEach(dto -> {
                    dto.setType(2);
                    UserBO tmpUser = userMapByComm.get(dto.getCommUid()).get(0);
                    dto.setName(tmpUser.getName());
                });

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

        //清除评论未读角标
        DynamicDataSourceContextHolder.push(DBType.CHAT.getDbName());
        ChatTop top = topMapper.selectById(userId);
        top.setCommentCount(0);
        topMapper.updateById(top);
        DynamicDataSourceContextHolder.poll();

        //获取用户头像
        List<Long> userIdTMP = new ArrayList();
        if (Objects.nonNull(userIds) && !userIds.isEmpty()) {
            userIdTMP.addAll(userIds);
        }

        if (Objects.nonNull(ids) && ids.isEmpty()) {
            userIdTMP.addAll(ids);
        }

        if (!userIdTMP.isEmpty()) {
            List<Auth.UserAPI.User> userList = api.getUserList(userIdTMP);
            Map<Long, String> avatarMap = userList
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(LambdaUtil.distinct(Auth.UserAPI.User::getId))
                    .collect(
                            Collectors.toMap(Auth.UserAPI.User::getId, u -> u.getAvatar()));

            page.getRecords().forEach(dto -> {
                String avatar = avatarMap.get(dto.getCommUid());
                dto.setAvatarPath(avatar);
            });
        }

        return page;
    }

    @Override
    public Page<String> getNick(Long userId, Integer current, Integer size) {
        //获取关注用户id
        DynamicDataSourceContextHolder.push(DBType.CONTENT.getDbName());
        MPJLambdaWrapper<Fan> userIdWrap = new MPJLambdaWrapper<Fan>()
                .select(Fan::getUserId)
                .eq(Fan::getFollowUserId, userId)
                .eq(Fan::getDeleteFlag, 0);

        List<Long> userIds = fanMapper.selectJoinList(Long.class, userIdWrap);
        DynamicDataSourceContextHolder.poll();

        if (Objects.isNull(userIds)) {
            return new Page();
        }

        //获取关注用户列表
        DynamicDataSourceContextHolder.push(DBType.AUTH.getDbName());
        MPJLambdaWrapper<User> userWrapByFavo = new MPJLambdaWrapper<User>()
                .select(User::getName)
                .in(User::getId, userIds);

        Page<String> page = userMapper.selectJoinPage(new Page(current, size), String.class, userWrapByFavo);
        DynamicDataSourceContextHolder.poll();

        return page;
    }

    @DS("content")
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

    @DS("chat")
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