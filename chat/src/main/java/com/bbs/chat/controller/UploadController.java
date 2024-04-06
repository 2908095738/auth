package com.bbs.chat.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import com.bbs.Result;
import com.bbs.chat.dto.FileDto;
import com.bbs.chat.service.FileService;
import com.bbs.chat.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@Slf4j
@RestController
@RequestMapping("/news")
public class UploadController {

    @Value("${news.image.path}")
    private String imagePath;

    @Value("${news.video.path}")
    private String videoPath;

    private final FileService fileService;


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
    public Result<List<String>> upload(@RequestParam("file") @NotNull(message = "上传文件不能为空！")List<MultipartFile> fileList,
                                       @RequestParam("newId") @NotNull(message = "内容id不能为空！")Long newId) {
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        Long createId = 1L;
        List<String> filePathList = new ArrayList<>();
        List<FileDto> compressionFileList = new ArrayList<>();
        List<FileDto> auditFileList = new ArrayList<>();
        try {
            for (int i = 0; i < fileList.size(); i++) {
                MultipartFile file = fileList.get(i);
                //文件的请求路径根据文件类型分类
                String type = FileTypeUtil.getType(file.getInputStream());
                String filePath = "";
                if(FileUtils.fileType.get(type)==1){//图片
                    filePath = imagePath + DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1"+(i+1)+"."+type;
                    disposeFile(file,filePath,filePathList,compressionFileList,auditFileList,newId,createId);
                }else if(FileUtils.fileType.get(type)==2){//视频
                    if(fileList.size()>5){
                        return Result.failed("上传失败，视频数量超过限制！");
                    }
                    filePath = videoPath + DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1"+(i+1)+"."+type;
                    disposeFile(file,filePath,filePathList,compressionFileList,auditFileList,newId,createId);
                }
            }
            if (CollUtil.isNotEmpty(compressionFileList)){
                //异步压缩
                fileService.compression(compressionFileList);
            }
            if (CollUtil.isNotEmpty(auditFileList)){
                //放入redis
                fileService.putAuditRedis(auditFileList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件上传失败:", e);
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


    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public Result<Boolean> delFile(@RequestParam("filePathList") @NotNull(message = "删除文件url不能为空！")List<String> filePathList,
                                   @NotNull(message = "内容id不能为空！")Long newId,
                                   @NotNull(message = "用户id不能为空！")Long userId) {
        fileService.delFiles(filePathList,newId,userId);
        return Result.success();

    }








    @Autowired
    public UploadController(FileService fileService) {
        this.fileService = fileService;
    }
}
