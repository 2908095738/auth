package com.bbs.content.service;

import com.bbs.content.dto.FileDto;

import java.util.List;

public interface FileService {
    void putCompressionQueue(List<FileDto> file);


    void putAuditRedis(List<FileDto> auditFileList);
}
