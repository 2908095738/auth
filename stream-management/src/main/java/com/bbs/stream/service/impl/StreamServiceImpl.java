package com.bbs.stream.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.Auth;
import com.bbs.api.auth.company.staff.ChildStaff;
import com.bbs.api.auth.company.staff.SearchChildStaff;
import com.bbs.stream.dto.StreamDto;
import com.bbs.stream.entity.Stream;
import com.bbs.stream.mapper.StreamMapper;
import com.bbs.stream.service.StreamService;
import com.bbs.stream.util.ThreadLocalUtil;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StreamServiceImpl extends MPJBaseServiceImpl<StreamMapper, Stream> implements StreamService {

    @DubboReference
    private SearchChildStaff searchChildStaff;

    @Resource
    private Auth.UserAPI userAPI;

    @Override
    public Result create(Long companyId, Stream stream) {
        //规避重复点击
        Stream dbEntity = getOne(new MPJLambdaWrapper<Stream>()
                .select(Stream::getId)
                .eq(Stream::getCreateUid, stream.getCreateUid())
                .eq(Stream::getType, stream.getType())
                .eq(Stream::getStatus, 1)
        );
        if (!ObjectUtils.isEmpty(dbEntity))
            return Result.success();

        boolean isDone = save(stream);
        if (isDone)
            return Result.success();
        else
            return Result.failed("create fail");
    }

    @Override
    public Page<StreamDto> list(Long companyId, Integer current, Integer size, Integer status, Integer type) {
        //获取下属信息
        Set<ChildStaff> downUserList = searchChildStaff.search(ThreadLocalUtil.getCurrentUserId(), companyId);
//        Map<Long, String> nameByIdOfUser = downUserList.stream().collect(Collectors.toMap(ChildStaff::getId, ChildStaff::getName));
        List<Long> userIds = downUserList.stream().map(ChildStaff::getId).collect(Collectors.toList());

        //获取分页
        Page<StreamDto> page = getPage(userIds, current, size, status, type);
        if (CollectionUtils.isEmpty(page.getRecords()))
            return page;

        //筛选原审批列表
//        List<StreamDto> records = getFixRecords(page.getRecords(), nameByIdOfUser);
//        page.setRecords(records);
//        if (CollectionUtils.isEmpty(page.getRecords()))
//            return page;

        //下属id列表
        Set<Long> uidsTmp = page.getRecords().stream()
                .map(StreamDto::getCreateUid)
                .collect(Collectors.toSet());
        List<Long> uids = new ArrayList();
        uids.addAll(uidsTmp);

        //返回列表的实例域赋值
        Map<Long, Auth.UserAPI.User> userByUid = userAPI.getUserList(uids).stream()
                .collect(Collectors.toMap(Auth.UserAPI.User::getId, u -> u));
        List<StreamDto> dtos = page.getRecords();
        for (StreamDto dto : dtos) {
            Auth.UserAPI.User now = userByUid.get(dto.getCreateUid());
            dto.setAvatar(now.getAvatar());
            dto.setName(now.getName());
        }

        return page;
    }

    private Page<StreamDto> getPage(List<Long> userIds, Integer current, Integer size, Integer status, Integer type) {
        /**
         * TODO 性能问题，因为先从DB查全表，再筛选数据。
         */
        MPJLambdaWrapper<Stream> wrapper = new MPJLambdaWrapper<Stream>()
                .select(Stream::getId, Stream::getCreateUid, Stream::getType)
                .select(Stream::getContent, Stream::getCreateTime, Stream::getStatus)
                .select(Stream::getLeadr)

                .in(Stream::getCreateUid, userIds)
                .orderByDesc(Stream::getCreateTime);

        if (!ObjectUtils.isEmpty(status))
            wrapper.eq(Stream::getStatus, status);
        if (!ObjectUtils.isEmpty(type))
            wrapper.eq(Stream::getType, type);

        return selectJoinListPage(new Page(current, size), StreamDto.class, wrapper);
    }

    private List<StreamDto> getFixRecords(List<StreamDto> oriList, Map<Long, String> nameByIdOfUser) {
        List<StreamDto> result = new ArrayList();
        for (StreamDto dto : oriList) {
            if (nameByIdOfUser.containsKey(dto.getCreateUid()))
                result.add(dto);
        }
        return result;
    }
}