package com.flandreyu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 启动完成后自动打开浏览器（打包运行时开启，见 application-prod.yaml 的 app.open-browser）。
 *
 * <p>
 * 不依赖 AWT：Spring Boot 默认以 headless 模式启动，Desktop 不可用，因此优先走系统命令。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.open-browser", havingValue = "true")
public class StartupBrowserOpener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${app.open-browser-delay-ms:1200}")
    private long delayMs;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String url = "http://localhost:" + port + contextPath + "/";
        log.info("GridDiary 已启动，浏览器访问：{}", url);
        // 稍等一下让 Tomcat 的监听端口完全就绪，避免浏览器打开时连接被拒
        Thread opener = new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                open(url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.warn("自动打开浏览器失败（可手动访问 {}）：{}", url, e.getMessage());
            }
        }, "open-browser");
        opener.setDaemon(true);
        opener.start();
    }

    private void open(String url) throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            new ProcessBuilder("cmd", "/c", "start", "", url).redirectErrorStream(true).start();
        } else if (os.contains("mac")) {
            new ProcessBuilder("open", url).start();
        } else {
            new ProcessBuilder("xdg-open", url).start();
        }
    }
}
