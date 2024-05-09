package com.bbs.file.api.clean;

import com.bbs.Result;
import com.bbs.file.util.minio.FileOpt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping
public class CleanTemporary {

    @Resource
    private FileOpt fileOpt;

    @RequestMapping("/clean/temporary")
    public Result<Boolean> clean(@RequestParam List<String> resourceIds) {
        return Result.of(fileOpt.removeList(resourceIds));
    }
}
