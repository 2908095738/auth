package com.bbs.financial.api.certificate.file;

import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.enums.dfs.BusinessCode;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import com.bbs.financial.entity.CertificateFile;
import com.bbs.financial.service.CertificateFileService;
import com.bbs.financial.util.LoginUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@RestController
@RequestMapping
public class UploadFile {

    @Resource
    private DFS.Upload upload;

    @Resource
    private CertificateFileService certificateFileService;

    @PostMapping("/certificate/file/upload")
    public Result<Boolean> upload(
            
            @RequestParam String certificateWord,
            @RequestParam Long no,
            @RequestParam String date,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long certificateId
    ) {
        Long loginSetId = LoginUser.getLoginSetId();
        String contentType = file.getContentType();
        DFS.Upload.VO vo = upload.uploadFile(
                BusinessCode.FINANCIAL_CERTIFICATE,
                ResourceType.FILE,
                contentType,
                file
        );
        Integer fileType = (StringUtils.isNotBlank(contentType) && contentType.indexOf("image") >= INTEGER_ZERO) ?
                FileType.IMAGE.getCode() : FileType.FILE.getCode();
        CertificateFile entity = new CertificateFile(
                file.getOriginalFilename(),
                loginSetId,
                certificateWord,
                no,
                new Date(Long.parseLong(date)),
                vo.getUrl(),
                vo.getResourceID(),
                fileType,
                contentType,
                LoginUser.getId()
        );
        if(nonNull(certificateId)) entity.setCertificateId(certificateId);
        return Result.success(certificateFileService.save(entity));
    }
}
