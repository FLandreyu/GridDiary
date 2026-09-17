package com.flandreyu.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * 接口文档配置（springdoc-openapi）
 * 访问：/swagger-ui.html（UI）、/v3/api-docs（JSON）
 * 注意：文档路径不在 /api/** 下，因此不会被 LoginInterceptor 拦截；
 * 在 UI 里调用需要登录的接口前，先在页面登录拿到 Session 即可。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gridDiaryOpenApi() {
        return new OpenAPI().info(new Info()
                .title("九宫格记忆网 API")
                .version("v1.0")
                .description("""
                        课程设计项目「九宫格记忆网」接口文档。

                        - 登录态基于 Session：先在页面登录，浏览器会带上 JSESSIONID
                        - 统一响应体：{ code, message, data }（200 成功 / 400 业务错误 / 401 未登录 / 404 不存在 / 403 无权限）
                        - 标注 @PublicApi 的接口允许匿名访问，其余需登录
                        """)
                .contact(new Contact().name("GridDiary")));
    }

    /** 按模块分组，便于在 Swagger UI 右上角切换 */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder().group("01-用户").pathsToMatch("/api/user/**").build();
    }

    @Bean
    public GroupedOpenApi diaryApi() {
        return GroupedOpenApi.builder().group("02-日记").pathsToMatch("/api/diary/**", "/api/upload/**").build();
    }

    @Bean
    public GroupedOpenApi socialApi() {
        // 评论与点赞挂在 /api/diary/{id}/comments、/api/diary/{id}/like 下，已包含在日记组
        return GroupedOpenApi.builder().group("03-私信").pathsToMatch("/api/message/**").build();
    }

    @Bean
    public GroupedOpenApi statsApi() {
        return GroupedOpenApi.builder().group("04-统计与打卡").pathsToMatch("/api/stats/**", "/api/checkin/**").build();
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder().group("00-全部接口").pathsToMatch("/api/**").build();
    }
}
