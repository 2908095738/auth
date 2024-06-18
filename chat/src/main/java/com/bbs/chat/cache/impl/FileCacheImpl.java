package com.bbs.chat.cache.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.bbs.chat.cache.FileCache;
import com.bbs.chat.dto.AuditNewDto;
import com.bbs.chat.dto.FileDto;
import com.bbs.chat.enums.RedisKeys;
import com.bbs.chat.util.FileUtils;
import com.bbs.chat.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bbs.chat.util.FileUtils.fileType;

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
    public void delAllFiles(List<String> filePathList, Long newId) {
        String json = (String)redisUtil.hashGet(RedisKeys.AUDIT_NEW_FIlE.key(), RedisKeys.NEW.key()+newId.toString());
        Map<String,String> map = StringUtils.isBlank(json)? new HashMap<>(): JSONUtil.toBean(json, HashMap.class);
        filePathList.forEach(localPath->{
            String orDefault = map.getOrDefault(localPath, null);
            if(StringUtils.isNotEmpty(orDefault)){
                //删除本地文件
                FileUtils.delteFile(orDefault);
            }
        });
        //刷新redis
        redisUtil.delHash(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.NEW.key()+newId);
        redisUtil.delHash(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId);
    }

    @Override
    public void delAuditFiles(List<String> fileLocalPathList, Long newId) {
        //删除redis
        redisUtil.delHash(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId);
    }


    @Override
    public void delFiles(List<String> filePathList, Long newId) {
        String json = (String)redisUtil.hashGet(RedisKeys.AUDIT_NEW_FIlE.key(), RedisKeys.NEW.key()+newId.toString());
        Map<String,String> map = StringUtils.isBlank(json)? new HashMap<>(): JSONUtil.toBean(json, HashMap.class);
        filePathList.forEach(localPath->{
            String orDefault = map.getOrDefault(localPath, null);
            if(StringUtils.isNotEmpty(orDefault)){
                //删除本地文件
                FileUtils.delteFile(orDefault);
                //从map中移除
                map.remove(localPath);
            }
        });
        //刷新redis
        redisUtil.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.NEW.key()+newId, JSON.toJSONString(map));
        redisUtil.hashIntr(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.AUDIT_FILE_SIZE.key()+newId,-1);
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
                    log.debug("filePathResult:"+filePathResult);
                    Map<String,String> filePathMap = StringUtils.isBlank(filePathResult)? new HashMap<>(): JSONUtil.toBean(filePathResult, HashMap.class);
                    //判断文件数量是否一致
                    log.debug("fileSize:"+fileSize);
                    if(fileSize == filePathMap.size()&&fileSize!=0){
                        String userId = (String) newContentMap.get(newId);
                        //userId格式：idUID
                        result.add(new AuditNewDto(userId,newId,new ArrayList<>(filePathMap.keySet())));
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
