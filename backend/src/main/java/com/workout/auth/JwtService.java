package com.workout.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JWT 签发与解析组件（基础设施层）。
 * 只处理 Token 编解码，不访问数据库、不校验密码。
 */
@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;

    /**
     * 注入 JWT 配置。
     */
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 为已认证用户签发 Bearer Token。
     *
     * @param userId   用户主键
     * @param username 用户名（写入 subject）
     * @return 紧凑 JWT 字符串
     */
    public String issueToken(Long userId, String username) {
        // 关键入口：只打 userId/username，禁止打印完整密钥
        log.info("[鉴权JWT] issueToken start userId={}, username={}", userId, username);
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireDays(), ChronoUnit.DAYS);
        // 使用配置密钥构造 HMAC 签名钥
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(key)
                .compact();
        // 关键出口：仅摘要 token 前缀便于排障
        log.info("[鉴权JWT] issueToken done userId={}, tokenPrefix={}", userId, tokenPrefix(token));
        return token;
    }

    /**
     * 校验签名并解析出当前用户主体。
     *
     * @param token 紧凑 JWT（不含 Bearer 前缀）
     * @return 已认证主体
     */
    public AuthPrincipal parseToken(String token) {
        // 关键入口：只打 token 前缀，禁止打印完整 Token
        log.info("[鉴权JWT] parseToken start tokenPrefix={}", tokenPrefix(token));
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Number uid = claims.get("uid", Number.class);
        String username = claims.getSubject();
        AuthPrincipal principal = new AuthPrincipal(uid.longValue(), username);
        // 关键实体：解析出的用户标识
        log.info("[鉴权JWT] parseToken done userId={}, username={}", principal.getUserId(), principal.getUsername());
        return principal;
    }

    /**
     * 截断 token 用于日志脱敏。
     */
    private String tokenPrefix(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 8) + "...";
    }
}
