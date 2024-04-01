package com.bbs.content.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bbs.content.dto.FileDto;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.service.FileService;
import com.bbs.content.util.FileUtils;
import com.bbs.content.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bbs.content.util.FileUtils.fileType;
import static java.util.stream.Collectors.toSet;

@Service
public class FileServiceImpl implements FileService {

    private RedisUtil redisUtil;


    @Async("excelBatchImport")
    @Override
    public void compression(List<FileDto> fileDtoList) {
        fileDtoList.forEach(fileDto->{
            MultipartFile file = fileDto.getFile();
            String filePath = fileDto.getFilePath();
            try {
                String type = FileTypeUtil.getType(file.getInputStream());
                if (fileType.get(type) == 1) {//图片
                    //图片压缩落地
                    file.transferTo(new File(filePath));
                    FileUtils.doWithPhoto(filePath);
                } else if (fileType.get(type) == 2) {//视频
                    //压缩落地
                    FileUtils.compressionVideo(FileUtils.multipartFileToFile(file), filePath);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        //放入redis
        putAuditRedis(fileDtoList);
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
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}
