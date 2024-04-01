package com.bbs.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bbs.content.dto.FileDto;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.mq.RabbitmqConfig;
import com.bbs.content.mq.RabbitmqSend;
import com.bbs.content.service.FileService;
import com.bbs.content.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class FileServiceImpl implements FileService {

    private RabbitmqSend send;

    private RedisUtil redisUtil;


    @Override
    public void putCompressionQueue(List<FileDto> file) {
        file.forEach(fileDto -> send.send( RabbitmqConfig.QUEUE_INFORM_WAIT_FILE, RabbitmqConfig.ROUTINGKEY_FILE_WAIT, JSON.toJSONString(fileDto) ));
    }

    @Override
    public void putAuditRedis(List<FileDto> auditFileList) {
        FileDto fileDto = auditFileList.get(0);
        String json = (String) redisUtil.hashGet(RedisKeys.AUDIT_NEWS_USERID.key()+fileDto.getCreateId().toString(),fileDto.getNewId().toString());
        Set<String> newFiles = StringUtils.isBlank(json)? new HashSet<>() : JSON.parseObject(json, new TypeReference<Set<String>>(){});
        newFiles.addAll(auditFileList.stream().map(FileDto::getFilePath).collect(toSet()));
        redisUtil.hashSet(RedisKeys.AUDIT_NEWS_USERID.key()+auditFileList.get(0).getCreateId().toString(),fileDto.getNewId().toString(),JSON.toJSONString(newFiles));
    }


    @Resource
    public void setSend(RabbitmqSend send) {
        this.send = send;
    }
    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}
