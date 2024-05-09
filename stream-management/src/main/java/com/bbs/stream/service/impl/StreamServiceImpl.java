package com.bbs.stream.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.api.Auth;
import com.bbs.stream.dto.StreamDto;
import com.bbs.stream.entity.Stream;
import com.bbs.stream.entity.UserCompany;
import com.bbs.stream.mapper.StreamMapper;
import com.bbs.stream.service.StreamService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StreamServiceImpl extends MPJBaseServiceImpl<StreamMapper, Stream> implements StreamService {
    @Resource
    private Auth.UserAPI userAPI;

    @Override
    public Result create(Long companyId, Stream stream) {
        Long topUserId = getTopUid(companyId, stream.getCreateUid());

        //规避重复点击
        Stream dbEntity = getOne(new MPJLambdaWrapper<Stream>()
                .select(Stream::getId)
                .eq(Stream::getCreateUid, stream.getCreateUid())
                .eq(Stream::getTopUid, topUserId)
                .eq(Stream::getType, stream.getType())
                .eq(Stream::getStatus, 1)
        );
        if (!ObjectUtils.isEmpty(dbEntity))
            return Result.success();

        stream.setTopUid(topUserId);
        boolean isDone = save(stream);
        if (isDone)
            return Result.success();
        else
            return Result.failed("create fail");
    }

    /**
     * 获取上级用户id
     *
     * @param companyId 公司id
     * @param createUid 创建用户id
     * @return
     */
    private Long getTopUid(Long companyId, Long createUid) {
        List<UserCompany> respList = getUCompany(companyId);
        Map<Long, UserCompany> userKeyMap = respList.stream().collect(Collectors.toMap(UserCompany::getUserId, item -> item));
        UserCompany now = userKeyMap.get(createUid);

        //TODO 如果该接口空指针什么的，把userCompanyKeyMap换成userKeyMap
        Long userCompanyId = now.getLead();
        Map<Long, UserCompany> userCompanyKeyMap = respList.stream().collect(Collectors.toMap(UserCompany::getId, item -> item));
        return userCompanyKeyMap.get(userCompanyId).getUserId();
    }

    /**
     * 获取职位列表
     *
     * @param companyId 公司id
     * @return
     */
    private List<UserCompany> getUCompany(Long companyId) {
        //TODO 从JSON获取数据的字符串硬编码待解决
        JSONObject jsonObject = JSON.parseObject(getResp(companyId));
        JSONObject da = (JSONObject) jsonObject.get("data");
        JSONObject sL = (JSONObject) da.get("staffList");
        JSONArray list = (JSONArray) sL.get("records");
        return list.toList(UserCompany.class);
    }

    private String getResp(Long companyId) {
        //TODO 分页、请求地址硬编码待解决
        Map<String, Object> reqParam = new HashMap();
        reqParam.put("id", companyId);
        reqParam.put("current", 1);
        reqParam.put("size", 2);
        return HttpUtil.get("http://192.168.0.100:8515/company/staff", reqParam);
    }

    @Override
    public Page<StreamDto> list(Integer current, Integer size, Integer status, Integer type, Long topUid) {
        MPJLambdaWrapper<Stream> wrapper = new MPJLambdaWrapper<Stream>()
                .select(Stream::getId, Stream::getCreateUid, Stream::getType)
                .select(Stream::getContent, Stream::getCreateTime, Stream::getStatus)

                .eq(Stream::getTopUid, topUid)
                .orderByDesc(Stream::getCreateTime);

        if (!ObjectUtils.isEmpty(status))
            wrapper.eq(Stream::getStatus, status);
        if (!ObjectUtils.isEmpty(type))
            wrapper.eq(Stream::getType, type);

        Page<StreamDto> page = selectJoinListPage(new Page(current, size), StreamDto.class, wrapper);

        if (CollectionUtils.isEmpty(page.getRecords()))
            return page;

        //用户头像赋值
        Set<Long> uidsTmp = page.getRecords().stream()
                .map(StreamDto::getCreateUid)
                .collect(Collectors.toSet());
        List<Long> uids = new ArrayList();
        uids.addAll(uidsTmp);

        Map<Long, List<Auth.UserAPI.User>> tmpMap = userAPI.getUserList(uids).stream()
                .collect(Collectors.groupingBy(Auth.UserAPI.User::getId));
        List<StreamDto> dtos = page.getRecords();
        for (StreamDto dto : dtos) {
            Auth.UserAPI.User now = tmpMap.get(dto.getCreateUid()).get(0);
            dto.setAvatar(now.getAvatar());
            dto.setName(now.getName());
        }

        return page;
    }
}