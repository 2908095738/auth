package com.bbs.chat.cache;

import com.bbs.chat.dto.AuditNewDto;
import com.bbs.chat.dto.FileDto;

import java.util.List;

public interface FileCache {
    void compression(List<FileDto> file);


    void putAuditRedis(List<FileDto> auditFileList);

    void delAllFiles(List<String> filePathList, Long newId);

    List<AuditNewDto> getAuditFile();

    void delFiles(List<String> filePathList, Long newId);

    void delAuditFiles(List<String> fileLocalPathList, Long newId);
}
