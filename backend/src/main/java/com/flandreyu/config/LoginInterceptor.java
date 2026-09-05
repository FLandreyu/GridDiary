package com.flandreyu.config;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.flandreyu.common.PublicApi;
import com.flandreyu.common.Result;
import com.flandreyu.common.SessionKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * 登录拦截器：
 * 1. OPTIONS 预检请求直接放行（CORS）；
 * 2. Session 有登录用户则放行；
 * 3. 标注了 @PublicApi 的接口允许匿名访问；
 * 4. 其余返回 401。
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // CORS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionKeys.LOGIN_USER) != null) {
            return true;
        }

        // 标注了 @PublicApi 的接口允许匿名访问
        if (handler instanceof HandlerMethod method
                && (method.hasMethodAnnotation(PublicApi.class)
                        || method.getBeanType().isAnnotationPresent(PublicApi.class))) {
            return true;
        }

        // 未登录：返回 401 + JSON
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "未登录或登录已过期")));
        return false;
    }
}
