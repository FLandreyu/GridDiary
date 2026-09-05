package com.flandreyu.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * Web 配置：CORS + 登录拦截器注册 + 上传目录静态映射
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
    }
}
