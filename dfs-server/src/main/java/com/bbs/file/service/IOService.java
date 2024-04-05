package com.bbs.file.service;

import com.bbs.file.conf.FastDFSConf;
import com.bbs.file.util.FastDFS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Service
public class IOService {

    @Resource
    private FastDFS util;
    @Resource
    private FastDFSConf conf ;

    /**
     * 上传
     * @param file 文件
     * @return 文件地址
     */
    public String upload(MultipartFile file){
        try {
            byte[] bytes = file.getBytes();
            String originalFileName = file.getOriginalFilename();
            assert originalFileName != null;
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            String fileName = file.getName();
            long fileSize = file.getSize();
            log.info("FastDFS：文件上传文件属性[originalFileName:{},fileName:{},fileSize:{},extension:{}, bytes.lengt:{}]",originalFileName,fileName,fileSize,extension,bytes.length);

            String url = util.uploadFile(bytes, fileSize, extension);
            String resultUrl = conf.getOuturl() + url;

            log.info("FastDFS：文件地址：{}",resultUrl);
            return resultUrl;
        } catch (IOException e) {
            log.error("FastDFS：上传文件失败！！！ Error={}", e.getMessage());
        }
        return null;
    }

    /**
     * 下载
     * @param url 文件URL
     * @return 文件字节
     */
    public byte[] download(String url) {
        return util.downloadFile(url);
    }

    /**
     * 删除
     * @param url 文件URL
     */
    public void delete(String url) {
        log.info("FastDFS：删除文件 url={}", url);
        util.deleteFile(url);
    }
}
