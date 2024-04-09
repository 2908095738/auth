package com.bbs.auth.app.avatar;

import com.bbs.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
@Slf4j
public class UploadAvatar {
    /**
     * 头像上传
     */
    @PostMapping("/file")
    public Result<Boolean> upload() {
        return null;
    }
}
