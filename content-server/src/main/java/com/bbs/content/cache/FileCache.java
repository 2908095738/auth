package com.bbs.content.cache;

import com.bbs.content.dto.AuditNewDto;
import com.bbs.content.dto.FileDto;

import java.util.List;

public interface FileCache {
    void compression(List<FileDto> file);

    void putAuditRedis(List<FileDto> auditFileList);

    List<AuditNewDto> getAuditFile();

    void delAuditFiles(List<String> fileLocalPathList, Long newId);
}
