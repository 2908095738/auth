package com.bbs.content.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import com.bbs.Result;
import com.bbs.content.dto.FileDto;
import com.bbs.content.service.FileService;
import com.bbs.content.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bbs.content.util.FileUtils.fileType;


@Slf4j
@RestController
@RequestMapping("/news")
public class UploadController {

    @Value("${news.image.path}")
    private String imagePath;

    @Value("${news.video.path}")
    private String videoPath;

    private FileService fileService;


    /**
     * 文件上传接口
     * 从redis中取文件唯一url
     * 根据文件大小
     * 判断是否压缩:入队，压缩，落地，入队
     * 返回url
     * @param fileList 文件
     * @return List<String>
     */
    @PostMapping("/upload")
    public Result<List<String>> upload(@RequestParam("file")List<MultipartFile> fileList, @RequestParam("newId")Long newId) {
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        Long createId = 1L;
        List<String> filePathList = new ArrayList<>();
        List<FileDto> compressionFileList = new ArrayList<>();
        List<FileDto> auditFileList = new ArrayList<>();
        log.info("文件上传:{}", fileList);
        for (int i = 0; i < fileList.size(); i++) {
            MultipartFile file = fileList.get(i);
            try {
                //文件的请求路径根据文件类型分类
                String type = FileTypeUtil.getType(file.getInputStream());
                String filePath = "";
                if(fileType.get(type)==1){//图片
                    filePath = imagePath + DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1"+(i+1)+"."+type;
                    disposeFile(file,filePath,filePathList,compressionFileList,auditFileList,newId,createId);
                }else if(fileType.get(type)==2){//视频
                    if(fileList.size()>5){
                        return Result.failed("上传失败，视频数量超过限制！");
                    }
                    filePath = videoPath + DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1"+(i+1)+"."+type;
                    disposeFile(file,filePath,filePathList,compressionFileList,auditFileList,newId,createId);
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件上传失败:", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (CollUtil.isNotEmpty(compressionFileList)){
            //放入队列
            fileService.putCompressionQueue(compressionFileList);
        }
        if (CollUtil.isNotEmpty(auditFileList)){
            //放入redis
            fileService.putAuditRedis(auditFileList);
        }
        return Result.success(filePathList);
    }


    private void disposeFile(MultipartFile file, String filePath, List<String> filePathList, List<FileDto> compressionFileList, List<FileDto> auditFileList, Long newId, Long createId) throws Exception {
        //判断大小，处理
        double size = file.getSize();
        if( size < FileUtils.MAX_ALLOWED_FILE_SIZE){
            //直接落地
            file.transferTo(new File(filePath));
            //添加到待审核列表
            auditFileList.add(new FileDto(newId,file,filePath,createId));
        }else {
            //添加到待压缩列表
            compressionFileList.add(new FileDto(newId,file,filePath,createId));
        }
        //删除源文件
        FileUtils.delteTempFile(FileUtils.multipartFileToFile(file));
        filePathList.add(filePath);
    }


    @Autowired
    public UploadController(FileService fileService) {
        this.fileService = fileService;
    }
}
