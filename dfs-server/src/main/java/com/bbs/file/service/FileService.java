package com.bbs.file.service;

import com.bbs.file.cache.FilePathCache;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

@Component
public class FileService {

    @Resource
    private FilePathCache cache;
    @Resource
    private IOService io;

    /**
     * 文件上传
     * @param resourceID 资源ID
     * @param file 文件
     * @throws IllegalArgumentException 目标文件已存在
     */
    public void upload(String resourceID, MultipartFile file) throws IllegalArgumentException {
        checkArgument(cache.notExists(resourceID), "目标文件已存在");
        String filePath = io.upload(file);
        cache.set(resourceID, filePath);
    }

    /**
     * 文件覆盖
     * @param resourceID 资源ID
     * @param file 新文件
     */
    public void cover(String resourceID, MultipartFile file) {
        String path = cache.get(resourceID);
        if(nonNull(path)) {
            io.delete(path);
        }
        path = io.upload(file);
        cache.set(resourceID, path);
    }

    /**
     * 文件移动
     * @param sourceResourceID 来源资源ID
     * @param targetResourceID 目标资源ID
     * @throws IllegalArgumentException 目标文件不存在
     */
    public void move(String sourceResourceID, String targetResourceID) throws IllegalArgumentException {
        String filePath = cache.get(sourceResourceID);
        checkArgument(nonNull(filePath), "目标文件不存在");
        cache.set(targetResourceID, filePath);
        cache.del(sourceResourceID);
    }

    /**
     * 删除文件
     * @param sourceResourceID 资源ID
     * @throws IllegalArgumentException 目标文件不存在
     */
    public void del(String sourceResourceID) {
        String filePath = cache.get(sourceResourceID);
        checkArgument(nonNull(filePath), "目标文件不存在");
        io.delete(filePath);
        cache.del(sourceResourceID);
    }

    /**
     * 获取文件
     * @param sourceResourceID 资源ID
     * @return 文件
     */
    public byte[] get(String sourceResourceID) {
        String filePath = cache.get(sourceResourceID);
        checkArgument(nonNull(filePath), "目标文件不存在");
        return io.download(filePath);
    }
}
