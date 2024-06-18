package com.bbs.content.cache.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.bbs.content.cache.FileCache;
import com.bbs.content.dto.AuditNewDto;
import com.bbs.content.dto.FileDto;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.util.FileUtils;
import com.bbs.content.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bbs.content.util.FileUtils.fileType;
@Slf4j
@Service
public class FileCacheImpl implements FileCache {

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
        Long newId = auditFileList.get(0).getNewId();
        String json = (String)redisUtil.hashGet(RedisKeys.AUDIT_NEW_FIlE.key(), RedisKeys.NEW.key()+newId.toString());
        Map<String,String> map = StringUtils.isBlank(json)? new HashMap<>(): JSONUtil.toBean(json, HashMap.class);
        map.putAll(auditFileList.stream().collect(Collectors.toMap(FileDto::getFileLocalPath, FileDto::getFilePath)));
        redisUtil.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.NEW.key()+newId, JSON.toJSONString(map));
    }


    @Override
    public void delAuditFiles(List<String> fileLocalPathList, Long newId) {
        //删除redis
        redisUtil.delHash(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId.toString());
    }



    @Override
    public List<AuditNewDto> getAuditFile() {
        List<AuditNewDto> result = new ArrayList<>();

        Map<Object,Object> json = redisUtil.hashGet(RedisKeys.AUDIT_NEW_FIlE.key());

        if(CollUtil.isNotEmpty(json)){
            Map<Object, Object> newContentMap = redisUtil.hashGet(RedisKeys.AUDIT_USERID_NEWS.key());
            json.keySet().forEach(redisKey->{
                //例如：“audit_file_size:12”包含“audit_file_size:”
                if(redisKey.toString().contains(RedisKeys.AUDIT_FILE_SIZE.key())){
                    //截取内容id
                    String newId = redisKey.toString().split(":")[1];
                    //获取数量
                    int fileSize = Integer.parseInt((String) json.get(redisKey));

                    //拼接对应文件的key:
                    String filePathResult = (String) json.get(RedisKeys.NEW.key() + newId);
                    log.debug("newId:{},filePathResult:{}",newId,filePathResult);

                    Set<String> filePathMap = StringUtils.isBlank(filePathResult)? new HashSet<>(): JSONUtil.toBean(filePathResult, new TypeReference<Set<String>>() {}, true);
                    //判断文件数量是否一致
                    log.debug("fileSize:"+fileSize);
                    if(fileSize == filePathMap.size()&&fileSize!=0){
                        String userId = (String) newContentMap.get(newId);
                        //userId格式：idUID
                        result.add(new AuditNewDto(userId,newId,new ArrayList<>(filePathMap)));
                    }
                }
            });
        }
        return result;
    }


    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}
