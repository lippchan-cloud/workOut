package com.workout.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 未认证入口（安全边界）。
 * 将 Spring Security 的 401 写成统一 API 信封，避免返回默认 HTML。
 */
@Component
public class JsonAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JsonAuthEntryPoint.class);

    private final ObjectMapper objectMapper;

    /**
     * 注入 JSON 序列化器。
     */
    public JsonAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 写出 HTTP 401 统一信封。
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        // 关键入口：记录未携带或无效 Token 的路径，不打印 Token
        log.info("[鉴权入口] unauthorized path={}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(401, "未登录或登录已过期"));
    }
}
