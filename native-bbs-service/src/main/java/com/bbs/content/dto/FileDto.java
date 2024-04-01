package com.bbs.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDto{

    private Long newId;
    private MultipartFile file;
    private String filePath;
    private Long createId;

}
