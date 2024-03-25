package com.bbs.controller;

import cn.hutool.core.io.FileTypeUtil;
import com.bbs.Result;
import com.bbs.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

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
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file")CommonsMultipartFile file) {
        //TODO        UserVO currentUser = ThreadLocalUtil.getCurrentUser();
        log.info("文件上传:{}", file);
        String originalFilename = file.getOriginalFilename();
        try {
            if (originalFilename != null) {
                //文件的请求路径根据文件类型分类
                String type = FileTypeUtil.getType(file.getInputStream());
                String filePath = "";
                if(fileType.get(type)==1){//图片
                    //构造新的文件名称
                    String objectName = "uId:"+1+"|"+ new Date() + originalFilename;
                    filePath = imagePath + objectName;
                    //图片上传
                    file.transferTo(new File(filePath));
                }else if(fileType.get(type)==2){//视频
                    //压缩前
                    File source = FileUtils.multipartFileToFile(file);
                    // 压缩后的文件路径
                    File target = new File(videoPath+"compUID:"+1+"|"+ new Date() +originalFilename);
                    //视频压缩
                    FileUtils.compre(source,target,2);
                }
                return Result.success(filePath);
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件上传失败:{}", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.failed("文件上传失败");
    }

}
