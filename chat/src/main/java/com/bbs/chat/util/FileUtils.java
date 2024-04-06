package com.bbs.chat.util;


import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FileUtils {

    public static double MAX_ALLOWED_FILE_SIZE = 5000000;

    public static Map<String,Integer> fileType = new HashMap<String,Integer>(){{
        put("jpg",1);
        put("gif",1);
        put("png",1);
        put("JPG",1);
        put("GIF",1);
        put("PNG",1);
        put("mp4",2);
        put("MP4",2);
    }};




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
     * 删除本地文件
     * @param filePath
     */
    public static void delteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) { // 要删除的文件不存在
            log.info("文件" + filePath + "不存在，删除失败！");
        } else { // 要删除的文件存在
            file.delete();// 删除文件
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
//        String newPath = source.getAbsolutePath().substring(0, source.getAbsolutePath().lastIndexOf(File.separator)).concat(File.separator).concat(picName).concat(".mp4");
        File target = new File(picName);
        try {
            MultimediaObject object = new MultimediaObject(source);
//            //判断大小，处理
//            int maxSize = 15;
//            double mb = Math.ceil(source.length() / 1048576);
//            int second = (int) object.getInfo().getDuration() / 1000;
//            BigDecimal bd = new BigDecimal(String.format("%.4f", mb / second));
//            System.out.println("开始压缩视频了--> 视频每秒平均 " + bd + " MB ");
            // 视频 > 5MB, 或者每秒 > 0.5 MB 才做压缩， 不需要的话可以把判断去掉
//            boolean temp = mb > maxSize || bd.compareTo(new BigDecimal(0.5)) > 0;
            long time = System.currentTimeMillis();
            // 音频编码属性配置
            AudioAttributes audio = new AudioAttributes();
            // 视频编码属性配置
            VideoAttributes video = new VideoAttributes();
            // 编码设置
            EncodingAttributes attr = new EncodingAttributes();
//            if(temp){
                audio.setCodec("aac");
                // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 56000 = 56kb)
                audio.setBitRate(236000 / 2);
                // 设置重新编码的音频流中使用的声道数（1 =单声道，2 = 双声道（立体声））
                audio.setChannels(2);
                // 采样率越高声音的还原度越好，文件越大
                audio.setSamplingRate(8000);


                // 设置编码
                video.setCodec("h264");
                //设置视频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 5600000 = 5600kb)
                video.setBitRate(1000000);
                // 设置视频帧率（帧率越低，视频会出现断层，越高让人感觉越连续）
                video.setFrameRate(40);
                //（视频质量）属性对转码后视频的大小有很大的影响，并且对转码的时间也有一定的影响，主要影响视频质量，参数类型是整形，数值越小表示质量越高
                video.setQuality(3);

                attr.setAudioAttributes(audio);
                attr.setVideoAttributes(video);
                // 设置线程数
                attr.setEncodingThreads(Runtime.getRuntime().availableProcessors() / 2);
//            }
            attr.setFormat("mp4");

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





    /**
     * 对图片进行原比例无损压缩,压缩后覆盖原图片
     *
     * @param path
     */
    public static void doWithPhoto(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        BufferedImage image = null;
        FileOutputStream os = null;
        try {
            image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage bfImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bfImage.getGraphics().drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            os = new FileOutputStream(path);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
            encoder.encode(bfImage);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //添加静态代码块
    static {
        try {
            FFmpegFrameGrabber.tryLoad();
            FFmpegFrameRecorder.tryLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取视频中的图片
     * @param inputStream 视频输入流
     * @return
     * @throws Exception
     */
    public static BufferedImage grabberVideoFramer(InputStream inputStream) throws Exception {
        // 最后获取到的视频的图片缓存
        BufferedImage bufferedImage = null;
        // Frame对象
        org.bytedeco.javacv.Frame frame = null;
        // 标识
        int flag = 0;
        FFmpegFrameGrabber fFmpegFrameGrabber = null;
        try {
            //获取视频文件
            fFmpegFrameGrabber = new FFmpegFrameGrabber(inputStream);
            fFmpegFrameGrabber.start();

            // 获取视频总帧数
            int ftp = fFmpegFrameGrabber.getLengthInFrames();

            //对视屏 帧数处理
            while (flag <= ftp) {
                frame = fFmpegFrameGrabber.grabImage();
                //对视频的第10帧进行处理
                if (frame != null && flag == 10) {
                    // 图片缓存对象
                    bufferedImage = FrameToBufferedImage(frame);
                    break;
                }
                flag++;
            }
        }finally {
            if(fFmpegFrameGrabber != null) {
                fFmpegFrameGrabber.stop();
                fFmpegFrameGrabber.close();
            }
        }
        return bufferedImage;
    }

    private static BufferedImage FrameToBufferedImage(org.bytedeco.javacv.Frame frame) {
        // 创建BufferedImage对象
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bufferedImage = converter.getBufferedImage(frame);
        return bufferedImage;
    }

}

