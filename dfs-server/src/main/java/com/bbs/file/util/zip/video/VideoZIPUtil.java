package com.bbs.file.util.zip.video;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import ws.schild.jave.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import static com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isNull;

@Slf4j
public class VideoZIPUtil {

    /**
     * 进制：1M 对应的 Byte
     */
    private static final Integer ONE_MB_BYTE = 1048576;

    /**
     * 视频压缩阈值（大于该阈值，需要进行压缩）
     */
    private static final Integer SIZE_THRESHOLD = 5;

    /**
     * 压缩阈值：视频平均每秒大小
     */
    private static final BigDecimal SEC_SIZE_THRESHOLD = new BigDecimal("0.5");

    /**
     * 【视频平均每秒大小（秒）】转 BigDecimal 的格式参数
     */
    private static final String VIDEO_SEC_SIZE_TO_BIG_DECIMAL_FORMAT = "%.4f";

    /**
     * 视频输出格式
     */
    private static final String VIDEO_OUTPUT_FORMAT = "mp4";

    public static File wsSchild(File sourceFile, File targetFile) {
        if (isNull(sourceFile)) return null;
        // 获取：视频文件大小
        double size = getFileSize(sourceFile);  //单位 MB，转 float 避免精度丢失警告
        // 获取：视频平均每秒的大小（单位：秒）
        double averageSECSize = getAverageSECSize(sourceFile, size);

        // 是否需要压缩（视频 & 音频）
        // 条件：大小 > 5MB or 每秒 > 0.5 MB 才做压缩
        if(fileIsTooBig(size) || fileAverageSECSizeTooBig(averageSECSize)){
            EncodingAttributes config = newConfig();

            // 输出文件：格式设置
            outputFormatConfig(config);

            // 音频配置
            config.setAudioAttributes(audioCodeConfig());

            // 视频配置
            config.setVideoAttributes(videoCodeConfig());

            // 线程数
            int needThreadNumber = halfCPUNumber();
            config.setEncodingThreads(needThreadNumber);

            log.info("压缩-视频: path={}; 大小={}; 平均每秒大小={}MB; 线程数={}", sourceFile.getPath(), size, averageSECSize, needThreadNumber);

            long time = System.currentTimeMillis();
            // 重编码：开始压缩
            zip(sourceFile, targetFile, config);
            log.info("压缩-视频: 压缩耗时={}", (System.currentTimeMillis() - time) / 1000);
        }
        return sourceFile;
    }

    private static void zip(File sourceFile, File target, EncodingAttributes config) {
        try {
            new Encoder().encode(new MultimediaObject(sourceFile), target, config);
            if(!sourceFile.delete()) throw new IOException();
        } catch (EncoderException e) {
            log.error("压缩-视频: 压缩异常！！！filePath={}", sourceFile.getPath());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("压缩-视频: 删除源文件失败！！！filePath={}", sourceFile.getPath());
        }
    }

    private static double getFileSize(File sourceFile) {
        return Math.ceil(1.0F * sourceFile.length() / ONE_MB_BYTE);
    }

    private static double getAverageSECSize(File source, double fileSize) {
        try {
            return fileSize / (int) new MultimediaObject(source).getInfo().getDuration() / 1000;
        } catch (EncoderException e) {
            log.error("压缩-视频: 获取视频详细信息失败！！！filePath={}", source.getPath());
            throw new RuntimeException(e);
        }
    }

    private static int halfCPUNumber() {
        return Runtime.getRuntime().availableProcessors() / NumberUtils.INTEGER_TWO;
    }

    /**
     * 重新编码（压缩）参数配置
     * @return 重新编码（压缩）的参数配置
     */
    private static EncodingAttributes newConfig() {
        return new EncodingAttributes();
    }

    /**
     * 输出格式配置
     * @param attr 重新编码（压缩）的参数配置
     */
    private static void outputFormatConfig(EncodingAttributes attr) {
        attr.setFormat(VIDEO_OUTPUT_FORMAT);
    }

    /**
     * 音频编码
     */
    private static final String AUDIO_CODE = "aac";
    /**
     * 音频编码属性配置
     * @return 视频编码属性配置
     */
    private static AudioAttributes audioCodeConfig() {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(AUDIO_CODE);
        // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 56000 = 56kb)
        audio.setBitRate(236000 / 2);
        // 设置重新编码的音频流中使用的声道数（1 =单声道，2 = 双声道（立体声））
        audio.setChannels(2);
        // 采样率越高声音的还原度越好，文件越大
        audio.setSamplingRate(8000);
        return audio;
    }

    /**
     * 视频编码
     */
    private static final String VIDEO_CODE = "h264";

    /**
     * 视频编码属性配置
     * @return 视频编码属性配置
     */
    private static VideoAttributes videoCodeConfig() {
        VideoAttributes video = new VideoAttributes();
        // 设置编码
        video.setCodec(VIDEO_CODE);
        //设置视频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 5600000 = 5600kb)
        video.setBitRate(1000000);
        // 设置视频帧率（帧率越低，视频会出现断层，越高让人感觉越连续）
        video.setFrameRate(40);
        //（视频质量）属性对转码后视频的大小有很大的影响，并且对转码的时间也有一定的影响，主要影响视频质量，参数类型是整形，数值越小表示质量越高
        video.setQuality(3);
        return video;
    }

    private static Boolean fileIsTooBig(double size) {
        return size > SIZE_THRESHOLD;
    }

    private static Boolean fileAverageSECSizeTooBig(double averageSECSize) {
        return
                new BigDecimal(String.format(VIDEO_SEC_SIZE_TO_BIG_DECIMAL_FORMAT, averageSECSize))
                        .compareTo(SEC_SIZE_THRESHOLD) > NumberUtils.INTEGER_ZERO;
    }
}
