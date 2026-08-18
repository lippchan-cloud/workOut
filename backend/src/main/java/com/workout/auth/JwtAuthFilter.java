package com.workout.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Bearer 过滤器（安全基础设施层）。
 * 从 Authorization 头解析 Token 并写入 SecurityContext；不查询数据库、不信任 body 中的 userId。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;

    /**
     * 注入 JWT 解析组件。
     */
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * 若请求携带 Bearer Token，则校验并设置当前认证主体。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();
            try {
                // 从签名 Token 取出 userId，禁止使用客户端传入的身份字段
                AuthPrincipal principal = jwtService.parseToken(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of());
                // 写入安全上下文供后续业务接口读取当前用户
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[鉴权过滤] authenticated userId={}, username={}, path={}",
                        principal.getUserId(), principal.getUsername(), request.getRequestURI());
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                log.info("[鉴权过滤] invalid token path={}, reason={}", request.getRequestURI(), ex.getClass().getSimpleName());
            }
        }
        filterChain.doFilter(request, response);
    }
}
