package com.bbs.file.controller;

import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.enums.dfs.FileType;
import com.bbs.file.util.minio.FileOpt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequestMapping
public class Upload {


    @Resource
    private FileOpt fileOpt;
    @PostMapping("/upload")
    public Result<DFS.Upload.VO> upload(
            @RequestParam String businessCode,
            @RequestParam Integer resourceType,
            @RequestParam(required = false) Integer fileType,
            @RequestParam(required = false) String contentType,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String resourceID = fileOpt.resourceID(businessCode, resourceType, nonNull(fileType) ? fileType : FileType.FILE.getCode());
            String url = fileOpt.upload(
                    resourceID,
                    file,
                    (nonNull(fileType) ? FileType.map.get(resourceType).getContentType() : (
                            nonNull(contentType) ? contentType : file.getContentType()
                    ))
            );
            return Result.success(new DFS.Upload.VO(url, resourceID));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private List<String> resourceIds;
    }

    @DeleteMapping("/batch")
    public Result<Boolean> deleteList(Param param){
        return Result.of(fileOpt.removeList(param.resourceIds));
    }
}
