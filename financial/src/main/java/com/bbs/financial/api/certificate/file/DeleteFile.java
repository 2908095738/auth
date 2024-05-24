package com.bbs.financial.api.certificate.file;

import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.financial.entity.CertificateFile;
import com.bbs.financial.service.CertificateFileService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping
@RestController
public class DeleteFile {

    @Resource
    private CertificateFileService certificateFileService;

    @Resource
    private DFS.Clean dfsCleanApi;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        private Long id;

        private List<Long> ids;
    }

    @DeleteMapping("/certificate/file")
    public Result<Boolean> del(@RequestBody Param param) {
        CertificateFile file = certificateFileService.getById(param.id);
        dfsCleanApi.batchClean(DFS.clean_api, Collections.singletonList(file.getResourceId()));
        return Result.success(certificateFileService.removeById(param.id));
    }

    @DeleteMapping("/certificate/file/batch")
    public Result<Boolean> delBatch(@RequestBody Param param) {
        dfsCleanApi.batchClean(
                DFS.clean_api,
                certificateFileService.listByIds(param.ids).stream().map(CertificateFile::getResourceId).collect(Collectors.toList())
        );
        return Result.success(certificateFileService.removeBatchByIds(param.ids));
    }
}
