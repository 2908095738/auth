package com.bbs.file.util.minio;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import com.bbs.file.conf.MinioConf;
import com.bbs.file.util.RedisUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class FileOpt {

    @Resource
    private MinioConf prop;

    @Resource
    private MinioClient minioClient;

    @Resource
    private RedisUtil redisUtil;

    private static final String FILE_INCR_NUM = "dfs:file:num:incr:";

    private static final String resource_id_prefix = "dfs";

    private static final String TIME_FORMAT = "yyyyMMddHHmm";

    private Long incr(String time) {
        return redisUtil.incr(FILE_INCR_NUM + time);
    }

    /**
     * 资源ID
     * @param businessCode 业务码：自定义(全局唯一)
     * @param resourceType 资源类型码：ResourceType
     * @param fileType 文件类型码：FileType
     * @return dfs + 服务码 + 业务码 + 资源类型码 + 文件类型码 + yyyyMMddHHmm + incrNum
     * yyyyMMddHHmm：年月日小时分钟
     * incrNum：Redis 基于当前分钟的递增值
     */
    public String resourceID(String businessCode, Integer resourceType, Integer fileType) {
        String nowTime = DateUtil.format(new Date(), TIME_FORMAT);
        return resource_id_prefix + businessCode + resourceType + fileType + nowTime + incr(nowTime);
    }

    /**
     * 文件上传
     * @param file 文件
     */
    public void upload(String resourceID, MultipartFile file, String contentType) {
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(prop.getBucketName()).object(resourceID)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(contentType).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 预览图片
     * @param resourceID 资源ID
     * @return 预览 URL
     */
    public String preview(String resourceID){
        // 查看文件地址
        GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder().bucket(prop.getBucketName()).object(resourceID).method(Method.GET).build();
        try {
            return minioClient.getPresignedObjectUrl(build);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件下载
     * @param resourceID 资源ID
     * @param res HttpResp
     */
    public void download(String resourceID, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(prop.getBucketName())
                .object(resourceID).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs)){
            byte[] buf = new byte[1024];
            int len;
            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()){
                while ((len=response.read(buf))!=-1){
                    os.write(buf,0,len);
                }
                os.flush();
                byte[] bytes = os.toByteArray();
                res.setCharacterEncoding("utf-8");
                // 设置强制下载不打开
                res.setContentType("application/force-download");
                res.addHeader("Content-Disposition", "attachment;fileName=" + resourceID);
                try (ServletOutputStream stream = res.getOutputStream()){
                    stream.write(bytes);
                    stream.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查看文件对象
     * @return 存储bucket内文件对象信息
     */
    public List<Item> listObjects() {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(prop.getBucketName()).build());
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }

    /**
     * 删除
     */
    public boolean remove(String resourceID){
        try {
            minioClient.removeObject( RemoveObjectArgs.builder().bucket(prop.getBucketName()).object(resourceID).build());
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
