package com.bbs.financial.api.certificate.file;

import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.enums.dfs.BusinessCode;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import com.bbs.financial.entity.CertificateFile;
import com.bbs.financial.service.CertificateFileService;
import com.bbs.financial.util.LoginUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping
public class UploadFile {

    @Resource
    private DFS.Upload upload;

    @Resource
    private CertificateFileService certificateFileService;

    @PostMapping("/certificate/file/upload")
    public Result<Boolean> upload(
            @RequestParam Long companyId,
            @RequestParam String certificateWord,
            @RequestParam Long no,
            @RequestParam String date,
            @RequestParam MultipartFile file
    ) {
        DFS.Upload.VO vo = upload.uploadFile(
                BusinessCode.FINANCIAL_CERTIFICATE,
                ResourceType.FILE,
                file.getContentType(),
                file
        );
        return Result.success(
                certificateFileService.save(
                        new CertificateFile(
                                file.getOriginalFilename(),
                                companyId,
                                certificateWord,
                                no,
                                new Date(Long.parseLong(date)),
                                vo.getUrl(),
                                vo.getResourceID(),
                                FileType.FILE.getCode(),
                                LoginUser.getId()
                        )
                )
        );
    }
}
