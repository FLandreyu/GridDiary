package com.flandreyu;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.flandreyu.mapper")
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BackendApplication.class);
		// 打包成 exe 运行时（jpackage 应用镜像）把应用根目录交给 Spring，
		// 上传目录、日志目录等都以它为基准解析；开发运行时不设置，沿用「相对当前工作目录」的老行为。
		File home = resolveAppHome();
		if (home != null) {
			app.setDefaultProperties(buildPackagedDefaults(home));
		} else if (System.getProperty("jpackage.app-path") != null) {
			// 走 jpackage 启动器却没识别出根目录：把线索打出来（调试版 exe 的控制台里能看到）
			System.out.println("[GridDiary] 未能识别应用根目录，jpackage.app-path="
					+ System.getProperty("jpackage.app-path") + "，将退回「当前工作目录」");
		}
		try {
			app.run(args);
		} catch (Throwable ex) {
			// 双击 exe 时没有控制台，启动失败必须留下可查看的线索
			StartupFailureReporter.report(home, ex);
			System.exit(1);
		}
	}

	/** 打包运行时的默认配置：应用根目录、可选的同级 config 目录、日志落盘位置 */
	private static Map<String, Object> buildPackagedDefaults(File home) {
		System.out.println("[GridDiary] 应用根目录：" + home);
		Map<String, Object> defaults = new HashMap<>();
		defaults.put("app.home", home.getAbsolutePath());

		// exe 同级目录若存在 config/application.yaml，则以它为准（便于换机器后改数据库密码/端口）
		File configDir = new File(home, "config");
		if (configDir.isDirectory()) {
			String location = configDir.getAbsolutePath().replace('\\', '/');
			defaults.put("spring.config.additional-location", "optional:file:" + location + "/");
		}

		// 双击启动没有控制台，日志必须落盘，否则启动失败将无迹可寻
		System.setProperty("logging.file.name",
				new File(new File(home, "logs"), "griddiary.log").getAbsolutePath());
		return defaults;
	}

	/**
	 * 识别「应用根目录」（jpackage 应用镜像的根，即 exe 所在目录）。
	 *
	 * <p>
	 * 优先使用 jpackage 启动器注入的 {@code jpackage.app-path}（指向启动器 exe 自身）；
	 * 取不到时再按「代码来源是名为 app 的目录下的 jar」推断，这条路径兜底给
	 * {@code java -jar <root>/app/backend.jar} 这类手工启动方式使用。
	 * 开发环境（target/classes）返回 null。
	 *
	 * <p>
	 * 注意：Spring Boot 可执行 jar 中类的 CodeSource 形如
	 * {@code jar:file:/…/app/backend.jar!/BOOT-INF/classes!/}，直接
	 * {@code new File(uri)}
	 * 会抛 IllegalArgumentException，所以按字符串截取 "!" 之前的部分。
	 */
	private static File resolveAppHome() {
		File fromLauncher = fromJpackageAppPath();
		return fromLauncher != null ? fromLauncher : fromCodeSource();
	}

	/** jpackage 的启动器会设置 jpackage.app-path 指向 exe 自身 */
	private static File fromJpackageAppPath() {
		String path = System.getProperty("jpackage.app-path");
		if (path == null || path.isBlank()) {
			return null;
		}
		File launcher = new File(path);
		File dir = launcher.isFile() ? launcher.getParentFile() : launcher;
		return dir != null && dir.isDirectory() ? dir : null;
	}

	private static File fromCodeSource() {
		try {
			URL source = BackendApplication.class.getProtectionDomain().getCodeSource().getLocation();
			if (source == null) {
				return null;
			}
			String path = source.getPath();
			int bang = path.indexOf('!'); // 截掉 jar 内部路径
			if (bang >= 0) {
				path = path.substring(0, bang);
			}
			if (path.indexOf('%') >= 0) {
				path = URLDecoder.decode(path, StandardCharsets.UTF_8); // 路径含中文/空格时的转义
			}
			// Windows 上 getPath() 会得到 /D:/xxx 这种形式，去掉盘符前多余的斜杠
			if (path.length() > 2 && path.charAt(0) == '/' && path.charAt(2) == ':') {
				path = path.substring(1);
			}
			File location = new File(path);
			File jarDir = location.isFile() ? location.getParentFile() : location;
			if (jarDir != null && "app".equals(jarDir.getName()) && jarDir.getParentFile() != null) {
				return jarDir.getParentFile();
			}
		} catch (Exception e) {
			// 识别失败就当作开发环境处理
		}
		return null;
	}
}
