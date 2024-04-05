package com.bbs.content.cache;

import com.bbs.content.dto.FileDto;

import java.util.List;

public interface FileCache {
    void compression(List<FileDto> file);


    void putAuditRedis(List<FileDto> auditFileList);

    void delFiles(List<String> filePathList, Long newId, Long userId);
}
