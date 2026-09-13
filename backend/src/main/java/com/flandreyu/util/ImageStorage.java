package com.flandreyu.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.flandreyu.common.BusinessException;

/**
 * 图片存储工具：保存原图 + 自动生成缩略图（不依赖第三方库，使用 JDK ImageIO）
 */
@Component
public class ImageStorage {

    /** 缩略图最长边（像素） */
    private static final int THUMB_MAX_EDGE = 400;

    @Value("${app.upload-dir:./upload}")
    private String uploadDir;

    /**
     * 保存一张图片
     *
     * @return 原图与缩略图的访问路径（以 /upload/... 相对 URL 存储）
     */
    public ImageUrls save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String format = resolveFormat(file.getContentType());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + format;

        File originalDir = new File(baseDir(), "original");
        File thumbDir = new File(baseDir(), "thumb");
        mkdirs(originalDir);
        mkdirs(thumbDir);

        File original = new File(originalDir, filename);
        File thumb = new File(thumbDir, filename);
        try {
            file.transferTo(original);
            createThumbnail(original, thumb);
        } catch (IOException e) {
            throw new BusinessException("图片保存失败：" + e.getMessage());
        }
        return new ImageUrls("/upload/original/" + filename, "/upload/thumb/" + filename);
    }

    /** 仅允许 jpg / png，返回文件后缀格式 */
    private String resolveFormat(String contentType) {
        if ("image/jpeg".equals(contentType)) {
            return "jpg";
        }
        if ("image/png".equals(contentType)) {
            return "png";
        }
        throw new BusinessException("仅支持 jpg / png 格式图片");
    }

    /** 等比缩略到最长边不超过 THUMB_MAX_EDGE */
    private void createThumbnail(File source, File target) throws IOException {
        BufferedImage src = ImageIO.read(source);
        if (src == null) {
            throw new BusinessException("文件不是有效的图片");
        }
        int maxEdge = Math.max(src.getWidth(), src.getHeight());
        int targetW;
        int targetH;
        if (maxEdge <= THUMB_MAX_EDGE) {
            targetW = src.getWidth();
            targetH = src.getHeight();
        } else {
            double ratio = (double) THUMB_MAX_EDGE / maxEdge;
            targetW = (int) (src.getWidth() * ratio);
            targetH = (int) (src.getHeight() * ratio);
        }
        BufferedImage thumb = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();

        String format = target.getName().substring(target.getName().lastIndexOf('.') + 1);
        ImageIO.write(thumb, format, target);
    }

    /**
     * 上传根目录：必须是**绝对路径**。
     * Tomcat 的 MultipartFile.transferTo(File) 遇到相对路径会把它解析到 multipart 临时目录下，
     * 导致「系统找不到指定的路径」。相对配置（./upload）按 JVM 工作目录展开后返回绝对 File。
     */
    private File baseDir() {
        return new File(uploadDir).getAbsoluteFile();
    }

    private void mkdirs(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败：" + dir.getAbsolutePath());
        }
    }

    /** 图片保存结果：原图路径 + 缩略图路径 */
    public record ImageUrls(String originalUrl, String thumbUrl) {
    }
}
