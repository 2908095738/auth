package com.bbs.file.util.zip.img;

import cn.hutool.core.img.Img;
import cn.hutool.core.io.FileUtil;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class ImgZIPUtil {

    /**
     * Hutool 图像压缩
     * @param source 目标绝对路径（例：e:/pic/1111.png）
     * @param target 输出绝对路径（只能输出 JPG！！！例：e:/pic/1111_target.jpg）
     * @param quality 压缩比（推荐 0.8）
     */
    public static Boolean hutool(String source, String target, int quality) {
        return Img.from(FileUtil.file(source))
                .setQuality(quality)//压缩比率
                .write(FileUtil.file(target));
    }

    /**
     * JPEGCodec 图像压缩
     * @param source 目标绝对路径
     * @param target 输出绝对路径（只能输出 JPG！！！）
     */
    public static void jpegCodec(String source, String target) {
        File file = new File(source);
        if (!file.exists()) {
            return;
        }
        BufferedImage image;
        FileOutputStream os = null;
        try {
            image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage bfImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bfImage.getGraphics().drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            os = new FileOutputStream(target);
            JPEGImageEncoder encoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(os);
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
}
