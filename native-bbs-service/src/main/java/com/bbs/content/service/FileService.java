package com.bbs.content.service;

import com.bbs.content.dto.FileDto;

import java.util.List;

public interface FileService {
    void compression(List<FileDto> file);


    void putAuditRedis(List<FileDto> auditFileList);
}
