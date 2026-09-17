import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * 生成 Windows 应用图标（.ico），图形与 frontend/public/images/logo.svg 一致：
 * 圆角方块 + 紫→粉→青 三色渐变 + 3x3 白色九宫格（右下角半透明）。
 *
 * <p>
 * 用法（JDK 11+ 单文件源码运行）：{@code java tools/MakeIcon.java tools/griddiary.ico}
 */
public final class MakeIcon {

    /** 设计稿基准尺寸（对应 logo.svg 的 viewBox 64x64） */
    private static final int BASE = 256;

    /** ICO 中内嵌的 PNG 尺寸 */
    private static final int[] SIZES = { 16, 32, 48, 64, 128, 256 };

    private MakeIcon() {
    }

    public static void main(String[] args) throws Exception {
        Path output = Paths.get(args.length > 0 ? args[0] : "tools/griddiary.ico");
        BufferedImage base = draw(BASE);

        List<byte[]> pngs = new ArrayList<>();
        for (int size : SIZES) {
            pngs.add(toPng(resize(base, size)));
        }

        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(output, buildIco(pngs));
        System.out.println("[make-icon] 已生成 " + output.toAbsolutePath() + "（含 " + SIZES.length + " 种尺寸）");
    }

    private static BufferedImage draw(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // logo.svg 的 64 单位坐标 -> 实际像素
        double k = size / 64.0;

        // 外层圆角方块 + 三色渐变
        g.setPaint(new LinearGradientPaint(
                new Point2D.Float(0, 0), new Point2D.Float(size, size),
                new float[] { 0f, 0.55f, 1f },
                new Color[] { new Color(0x7c6cff), new Color(0xff7abd), new Color(0x3fd8ff) }));
        g.fill(new RoundRectangle2D.Double(2 * k, 2 * k, 60 * k, 60 * k, 32 * k, 32 * k));

        // 九宫格
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                boolean highlightOff = row == 2 && col == 2;
                g.setColor(new Color(255, 255, 255, highlightOff ? 140 : 235));
                double x = (14 + col * 13) * k;
                double y = (14 + row * 13) * k;
                g.fill(new RoundRectangle2D.Double(x, y, 10 * k, 10 * k, 6 * k, 6 * k));
            }
        }

        g.dispose();
        return image;
    }

    /** 逐级折半缩放，避免一次性大幅缩小导致细节糊掉 */
    private static BufferedImage resize(BufferedImage src, int size) {
        BufferedImage current = src;
        while (current.getWidth() / 2 >= size) {
            current = scaleOnce(current, current.getWidth() / 2);
        }
        return current.getWidth() == size ? current : scaleOnce(current, size);
    }

    private static BufferedImage scaleOnce(BufferedImage src, int size) {
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, size, size, null);
        g.dispose();
        return dst;
    }

    private static byte[] toPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write(image, "png", buffer);
        return buffer.toByteArray();
    }

    /** 组装 ICO 容器：ICONDIR + 每个尺寸一条 ICONDIRENTRY，图像数据直接使用 PNG（Vista 起支持） */
    private static byte[] buildIco(List<byte[]> pngs) throws IOException {
        int count = pngs.size();
        int offset = 6 + 16 * count;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeShort(out, 0); // reserved
        writeShort(out, 1); // type: 1 = icon
        writeShort(out, count);

        for (int i = 0; i < count; i++) {
            int size = SIZES[i];
            byte[] png = pngs.get(i);
            out.write(size >= 256 ? 0 : size); // 宽（0 表示 256）
            out.write(size >= 256 ? 0 : size); // 高
            out.write(0); // 调色板数量
            out.write(0); // reserved
            writeShort(out, 1); // color planes
            writeShort(out, 32); // 位深
            writeInt(out, png.length);
            writeInt(out, offset);
            offset += png.length;
        }
        for (byte[] png : pngs) {
            out.write(png);
        }
        return out.toByteArray();
    }

    private static void writeShort(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }
}
