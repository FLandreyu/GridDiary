package com.flandreyu;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 启动失败时的兜底提示：把异常写进「启动失败.txt」并尝试用系统默认程序打开。
 *
 * <p>
 * 打包成 exe 双击运行时没有控制台，如果数据库没启动或密码不对，
 * 用户会只看到一个窗口一闪而过，这里保证错误信息一定能被看到。
 */
final class StartupFailureReporter {

    private StartupFailureReporter() {
    }

    static void report(File appHome, Throwable ex) {
        String text = buildMessage(ex);
        System.err.println(text);

        File dir = new File(appHome == null ? new File(".").getAbsoluteFile() : appHome, "logs");
        try {
            if (dir.mkdirs() || dir.isDirectory()) {
                File file = new File(dir, "启动失败.txt");
                Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
                openWithDefaultApp(file);
            }
        } catch (Exception ignored) {
            // 兜底逻辑本身不再抛异常
        }
    }

    private static String buildMessage(Throwable ex) {
        StringWriter buffer = new StringWriter();
        ex.printStackTrace(new PrintWriter(buffer));
        String stack = buffer.toString();

        StringBuilder message = new StringBuilder();
        message.append("GridDiary 启动失败\n");
        message.append("时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");
        message.append("常见原因排查：\n");
        message.append("  1. MySQL 服务未启动（Windows 服务里启动 MySQL80，或运行 net start MySQL80）\n");
        message.append("  2. 数据库账号密码不对：修改本目录下 config/application.yaml 里的 spring.datasource.password\n");
        message.append("  3. 端口 8080 被占用：换个端口 server.port，或关掉已运行的 GridDiary\n");
        message.append("  4. 数据库不存在：程序会自动创建 grid_diary 库与数据表，但需要账号有建库权限\n\n");
        message.append("错误详情：\n").append(stack);
        return message.toString();
    }

    private static void openWithDefaultApp(File file) {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", file.getAbsolutePath())
                        .redirectErrorStream(true).start();
            } else if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {
            // 打不开就算了，文件已经生成
        }
    }
}
