package com.workout.modules.auth.api;

import com.workout.common.UnauthorizedException;
import com.workout.modules.auth.domain.AuthPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户解析（接口层支撑）。
 * 只从 SecurityContext / JWT 取身份，禁止读取客户端传入的 userId。
 */
public final class CurrentUser {

    private static final Logger log = LoggerFactory.getLogger(CurrentUser.class);

    /**
     * 工具类禁止实例化。
     */
    private CurrentUser() {}

    /**
     * 读取当前请求的认证主体。
     *
     * @return AuthPrincipal
     */
    public static AuthPrincipal require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            log.info("[当前用户] require failed unauthenticated=true");
            throw new UnauthorizedException("未登录或登录已过期");
        }
        log.info("[当前用户] require start userId={}, username={}", principal.getUserId(), principal.getUsername());
        return principal;
    }

    /**
     * 尝试读取当前认证主体；匿名请求返回 null，不抛 401。
     *
     * @return AuthPrincipal 或 null
     */
    public static AuthPrincipal find() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            log.info("[当前用户] find unauthenticated=true");
            return null;
        }
        log.info("[当前用户] find start userId={}, username={}", principal.getUserId(), principal.getUsername());
        return principal;
    }
}
