package com.bbs.chat.service;


import com.bbs.chat.dto.FileDto;

import java.util.List;

public interface FileService {
    void compression(List<FileDto> file);


    void putAuditRedis(List<FileDto> auditFileList);

    void delFiles(List<String> filePathList, Long newId, Long userId);
}
