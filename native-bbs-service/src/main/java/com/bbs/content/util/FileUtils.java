package com.bbs.content.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.VideoAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
@Slf4j
@Component
public class FileUtils {

    public static double MAX_ALLOWED_FILE_SIZE = 5000000;


    public static File multipartFileToFile(MultipartFile file) throws Exception {
        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }


    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * 删除本地临时文件
     * @param file
     */
    public static void delteTempFile(File file) {
        if (file != null) {
            File del = new File(file.toURI());
            del.delete();
        }
    }


    /**
     * 传视频File对象，返回压缩后File对象信息
     *
     * @param source
     */
    public static File compressionVideo(File source, String picName) {
        if (source == null) {
            return null;
        }
        String newPath = source.getAbsolutePath().substring(0, source.getAbsolutePath().lastIndexOf(File.separator)).concat(File.separator).concat(picName).concat(".mp4");
        File target = new File(newPath);
        try {
            long time = System.currentTimeMillis();
            // 音频编码属性配置
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("aac");
            // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 56000 = 56kb)
            audio.setBitRate(236000 / 2);
            // 设置重新编码的音频流中使用的声道数（1 =单声道，2 = 双声道（立体声））
            audio.setChannels(2);
            // 采样率越高声音的还原度越好，文件越大
            audio.setSamplingRate(8000);

            // 视频编码属性配置
            VideoAttributes video = new VideoAttributes();
            // 设置编码
            video.setCodec("h264");
            //设置视频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 5600000 = 5600kb)
            video.setBitRate(1000000);
            // 设置视频帧率（帧率越低，视频会出现断层，越高让人感觉越连续）
            video.setFrameRate(40);
            //（视频质量）属性对转码后视频的大小有很大的影响，并且对转码的时间也有一定的影响，主要影响视频质量，参数类型是整形，数值越小表示质量越高
            video.setQuality(3);

            // 编码设置
            EncodingAttributes attr = new EncodingAttributes();
            attr.setFormat("mp4");
            attr.setAudioAttributes(audio);
            attr.setVideoAttributes(video);

            // 设置线程数
            attr.setEncodingThreads(Runtime.getRuntime().availableProcessors() / 2);

            Encoder encoder = new Encoder();
            MultimediaObject multimediaObject = new MultimediaObject(source);
            encoder.encode(multimediaObject, target, attr);
            log.debug("压缩总耗时：" + (System.currentTimeMillis() - time) / 1000);
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (target.length() > 0) {
                source.delete();
            }
        }
        return source;
    }


}

