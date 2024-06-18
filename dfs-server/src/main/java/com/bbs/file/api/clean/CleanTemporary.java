package com.bbs.file.api.clean;

import com.bbs.Result;
import com.bbs.file.util.minio.FileOpt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class CleanTemporary {

    @Resource
    private FileOpt fileOpt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {

        private List<String> resourceIds;
    }

    @DeleteMapping("/clean/temporary")
    public Result<Boolean> clean(@RequestBody Param param) {
        return Result.of(fileOpt.removeList(param.resourceIds));
    }
}
