package com.flandreyu.config;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import lombok.RequiredArgsConstructor;

/**
 * Web 配置：CORS + 登录拦截器注册 + 上传目录静态映射 + 前端单页应用兜底
 * 说明：公开接口统一用 @PublicApi 标记，拦截器按注解放行，这里无需维护白名单。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Value("${app.upload-dir:./upload}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 前后端分离开发期允许跨域；生产走 Vite/Nginx 同源代理
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器：@PublicApi 接口与 OPTIONS 在拦截器内部放行
        registry.addInterceptor(loginInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 图片访问路径 /upload/** -> 上传目录（映射到磁盘绝对路径）
        String location = new File(uploadDir).getAbsoluteFile().toURI().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/upload/**").addResourceLocations(location);

        // 前端构建产物（package.ps1 会把 frontend/dist 复制到 classpath:/static/）
        // 兜底 resolver 让 Vue Router 的 history 模式在直接访问 /diary/1 这类地址时也能正常刷新；
        // 更具体的路径（/upload/**、springdoc 的 /swagger-ui/**）由各自更精确的映射优先匹配。
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaFallbackResolver());
    }

    /**
     * 静态资源解析器：真实文件优先；找不到且不像后端接口/静态资源时，回退到 SPA 入口 index.html。
     */
    private static class SpaFallbackResolver extends PathResourceResolver {

        /** 由后端自己处理、不能回退到前端页面的路径前缀 */
        private static final String[] BACKEND_PREFIXES = { "api/", "upload/", "v3/", "swagger", "error" };

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            if (resourcePath.isEmpty()) {
                // 根路径 "/" 直接给首页，避免把目录资源当成文件返回
                return indexHtmlOrNull();
            }
            Resource requested = location.createRelative(resourcePath);
            if (requested.exists() && requested.isReadable()) {
                return requested;
            }
            // 带扩展名的资源（.js/.css/.png…）找不到就是真的 404，不做回退
            if (resourcePath.indexOf('.') >= 0 || isBackendPath(resourcePath)) {
                return null;
            }
            // 其余无扩展名的路径视为前端路由，回退到 SPA 入口
            return indexHtmlOrNull();
        }

        /** 未打包前端资源时返回 null，避免开发期掩盖真正的 404 */
        private Resource indexHtmlOrNull() {
            Resource index = new ClassPathResource("/static/index.html");
            return index.exists() ? index : null;
        }

        private boolean isBackendPath(String path) {
            for (String prefix : BACKEND_PREFIXES) {
                if (path.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }
    }
}
