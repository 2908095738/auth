package com.bbs.content.cache.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bbs.content.cache.FileCache;
import com.bbs.content.dto.AuditNewDto;
import com.bbs.content.dto.FileDto;
import com.bbs.content.enums.RedisKeys;
import com.bbs.content.util.FileUtils;
import com.bbs.content.util.RedisUtil;
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

import static com.bbs.content.util.FileUtils.fileType;

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
        Map<String,String> map = StringUtils.isBlank(json)? new HashMap<>(): JSON.parseObject(json, new TypeReference<Map<String,String>>(){});
        map.putAll(auditFileList.stream().collect(Collectors.toMap(FileDto::getFileLocalPath, FileDto::getFilePath)));
        redisUtil.hashSet(RedisKeys.AUDIT_NEW_FIlE.key(),RedisKeys.NEW.key()+newId, JSON.toJSONString(map));
    }

    @Override
    public void delFiles(List<String> fileLocalPathList, Long newId) {
        String json = (String)redisUtil.hashGet(RedisKeys.AUDIT_NEW_FIlE.key(), RedisKeys.NEW.key()+newId.toString());
        Map<String,String> map = StringUtils.isBlank(json)? new HashMap<>(): JSON.parseObject(json, new TypeReference<Map<String,String>>(){});
        fileLocalPathList.forEach(localPath->{
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
                    String newId = redisKey.toString().split(":")[0];
                    //获取数量
                    Integer fileSize = (Integer) json.get(redisKey);
                    //拼接对应文件的key:
                    String filePathResult = (String) json.get(RedisKeys.NEW.key() + newId);
                    Map<String,String> filePathMap = StringUtils.isBlank(filePathResult)? new HashMap<>(): JSON.parseObject(filePathResult, new TypeReference<Map<String,String>>(){});
                    //判断文件数量是否一致
                    if(fileSize == filePathMap.size()&&fileSize!=0){
                        String userIdContent = (String) newContentMap.get(newId);
                        //userIdContent格式：idUIDcontent
                        String[] userIdContentArray = userIdContent.split("UID");
                        result.add(new AuditNewDto(userIdContentArray[0],newId,userIdContentArray[1],new ArrayList<>(filePathMap.keySet())));
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
