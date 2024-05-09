package com.bbs.content.api.content;

import cn.hutool.http.ContentType;
import com.bbs.Result;
import com.bbs.api.DFS;
import com.bbs.content.util.FileUtils;
import com.bbs.enums.dfs.BusinessCode;
import com.bbs.enums.dfs.FileType;
import com.bbs.enums.dfs.ResourceType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;

import static com.bbs.content.enums.RedisKeys.CONTENT_FILE_UPLOAD_TMP;

@RestController
@RequestMapping
public class VideoCover {

    @Resource
    private DFS.Upload upload;

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> protoStuffTemplate;

    @PostMapping("/converter/img/video")
    public Result<String> getCover(MultipartFile file) throws Exception {
        BufferedImage bufferedImage = FileUtils.grabberVideoFramer(
                Files.newInputStream(FileUtils.multipartFileToFile(file).toPath())
        );
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
        MultipartFile image = new MockMultipartFile(
                ContentType.OCTET_STREAM.getValue(),
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray())
        );
        DFS.Upload.VO vo = upload.uploadTemporaryFile(
                BusinessCode.CONTENT_VIDEO_COVER_TMP,
                ResourceType.FILE,
                FileType.IMAGE,
                image
        );
        protoStuffTemplate.opsForSet().add(CONTENT_FILE_UPLOAD_TMP.key(), vo.getResourceID());
        return Result.success(vo.getUrl());
    }
}
