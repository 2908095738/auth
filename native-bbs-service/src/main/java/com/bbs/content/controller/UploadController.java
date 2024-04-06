package com.bbs.content.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import com.alibaba.fastjson.JSON;
import com.bbs.Result;
import com.bbs.content.cache.FileCache;
import com.bbs.content.dto.FileDto;
import com.bbs.content.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bbs.content.util.FileUtils.fileType;


@Slf4j
@RestController
@RequestMapping("/file")
public class UploadController {

    @Value("${news.image.path}")
    private String imagePath;

    @Value("${news.video.path}")
    private String videoPath;

    @Value("${news.image.down.prefix}")
    private String imageDownPrefix;


    @Value("${news.video.down.prefix}")
    private String videoDownPrefix;


    private final FileCache fileCache;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class FileInfo {
        private String name;//"平台战略.png"
        private String type;//"image/png"
    }

    /**
     * 文件上传接口
     * 从redis中取文件唯一url
     * 根据文件大小
     * 判断是否压缩:入队，压缩，落地，入队
     * 返回url
     * @param fileList 文件
     * @return List<String>
     */
    @PostMapping()
    public Result<List<String>> upload(@RequestParam("file") @NotNull(message = "上传文件不能为空！")List<MultipartFile> fileList,
                                       @RequestParam("newId") @NotNull(message = "内容id不能为空！")Long newId,
                                       String infos) {
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        Long createId = 1L;
        List<String> filePathList = new ArrayList<>();
        List<FileDto> compressionFileList = new ArrayList<>();
        List<FileDto> auditFileList = new ArrayList<>();
        try {
            for (int i = 0; i < fileList.size(); i++) {
                MultipartFile file = fileList.get(i);
                //文件的请求路径根据文件类型分类
                String type = FileTypeUtil.getType(file.getInputStream(),file.getName());
                if(StringUtils.isEmpty(type)){
                    List<FileInfo> fileInfos = JSON.parseArray(infos, FileInfo.class);
                    String name = fileInfos.get(i).name;
                    type = name.substring(name.lastIndexOf('.') + 1).trim();
                }
                String fileName = "";
                String filePath = "";
                if(fileType.get(type)==1){//图片
                    fileName= DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1"+(i+1)+"."+type;
                    filePath = imagePath + fileName;
                    disposeFile(file,filePath,compressionFileList,auditFileList,newId,createId);
                    filePathList.add(imageDownPrefix +"/" + fileName);
                }else if(fileType.get(type)==2){//视频
                    if(fileList.size()>5){
                        return Result.failed("上传失败，视频数量超过限制！");
                    }
                    fileName = DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1"+(i+1);
                    filePath = videoPath + fileName+"."+type;
                    disposeFile(file,filePath,compressionFileList,auditFileList,newId,createId);

                    String fmName = getVideoOneImage(fileName,file);

                    filePathList.add(videoDownPrefix +"/" + fileName);
                    filePathList.add(imageDownPrefix +"/" + fmName);
                }
            }
            if (CollUtil.isNotEmpty(compressionFileList)){
                //压缩
                fileCache.compression(compressionFileList);
            }
            if (CollUtil.isNotEmpty(auditFileList)){
                //放入redis
                fileCache.putAuditRedis(auditFileList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件上传失败:", e);
        }
        return Result.success(filePathList);
    }

    /**
     * 获取视频第一帧图片
     * @param fileName 第一帧图片名称
     * @param file 源视频文件
     * @return String
     * @throws Exception 异常
     */
    private String getVideoOneImage(String fileName, MultipartFile file) throws Exception {
        String fmName="FM"+fileName+".jpg";
        String fmPath = imagePath+fmName;
        BufferedImage bufferedImage = FileUtils.grabberVideoFramer(
                Files.newInputStream(FileUtils.multipartFileToFile(file).toPath())
        );
        ImageIO.write(bufferedImage, "jpg", new File(fmPath));
        return fmName;
    }

    /**
     * 处理文件
     * @param file 源文件
     * @param filePath 要落地的路径
     * @param compressionFileList 待压缩列表
     * @param auditFileList 待审核列表
     * @param newId 内容id
     * @param createId 创建人id
     * @throws Exception 异常
     */
    private void disposeFile(MultipartFile file,
                             String filePath,
                             List<FileDto> compressionFileList,
                             List<FileDto> auditFileList,
                             Long newId,
                             Long createId) throws Exception {
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
    }


    /**
     * 删除文件
     */
    @DeleteMapping()
    public Result<Boolean> delFile(@RequestParam("filePathList") @NotNull(message = "删除文件url不能为空！")List<String> filePathList,
                                   @NotNull(message = "内容id不能为空！")Long newId,
                                   @NotNull(message = "用户id不能为空！")Long userId) {
        fileCache.delFiles(filePathList,newId,userId);
        return Result.success();

    }








    @Autowired
    public UploadController( FileCache fileCache) {
        this.fileCache = fileCache;
    }
}
