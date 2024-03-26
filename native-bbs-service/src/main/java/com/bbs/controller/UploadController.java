package com.bbs.controller;

import cn.hutool.core.io.FileTypeUtil;
import com.bbs.Result;
import com.bbs.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/news")
public class UploadController {

    @Value("${news.image.path}")
    private String imagePath;

    @Value("${news.video.path}")
    private String videoPath;


    private Map<String,Integer> fileType = new HashMap<String,Integer>(){{
        put("jpg",1);
        put("gif",1);
        put("png",1);
        put("JPG",1);
        put("GIF",1);
        put("PNG",1);
        put("mp4",2);
        put("MP4",2);
    }};


    /**
     * 文件上传接口
     * 从redis中取文件唯一url
     * 根据文件大小
     * 判断是否压缩:入队，压缩，落地，入队
     * 返回url
     *
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        log.info("文件上传:{}", file);
        String originalFilename = file.getOriginalFilename();
        try {

            //文件的请求路径根据文件类型分类
            String type = FileTypeUtil.getType(file.getInputStream());
            String filePath = "";
            if(fileType.get(type)==1){//图片
                filePath = imagePath + DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1."+type;
            }else if(fileType.get(type)==2){//视频
                filePath = videoPath + DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1."+type;
            }

            //判断大小，处理
            double size = file.getSize();
            if( size < FileUtils.MAX_ALLOWED_FILE_SIZE){
                //直接落地
                file.transferTo(new File(filePath));
                }else {
                //压缩落地
                FileUtils.compressionVideo(FileUtils.multipartFileToFile(file), "native-bbs-service\\src\\main\\resources\\nvideo\\"+DateFormatUtils.format(new Date(),"YYYYMMDDHHmmss")+"UID1");
            }
            //删除源文件
            FileUtils.delteTempFile(FileUtils.multipartFileToFile(file));
            return Result.success(filePath);

        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件上传失败:{}", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.failed("文件上传失败");
    }















}
