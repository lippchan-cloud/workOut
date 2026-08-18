package com.workout.auth;

import com.workout.common.BusinessException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务（应用层）。
 * 负责注册编排：唯一性校验、密码哈希、落库、签发 JWT；不暴露仓储细节给 Controller。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 注入注册所需依赖。
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 注册新用户并立即签发 Token。
     *
     * @param username 唯一用户名
     * @param rawPassword 明文密码（不会落库）
     * @return token 与用户标识
     */
    @Transactional
    public AuthTokenResponse register(String username, String rawPassword) {
        long startMs = System.currentTimeMillis();
        // 关键入口：只记录用户名，禁止打印明文密码
        log.info("[鉴权注册] register start username={}", username);
        // 业务规则：用户名全站唯一
        if (userRepository.existsByUsername(username)) {
            log.error("[鉴权注册] register failed code=400 msg=该用户名已被注册 username={}", username);
            throw new BusinessException("该用户名已被注册");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username.trim());
        // 明文转 BCrypt 哈希后再持久化
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(Instant.now());
        // 写入用户行并回填自增主键
        UserEntity saved = userRepository.save(user);
        // 关键实体：落库后的用户标识
        log.info("[鉴权注册] saved entityType=UserEntity id={}, username={}", saved.getId(), saved.getUsername());

        // 注册成功即发 JWT，避免再调一次登录
        String token = jwtService.issueToken(saved.getId(), saved.getUsername());
        log.info(
                "[鉴权注册] register done success=true userId={}, elapsedMs={}",
                saved.getId(),
                System.currentTimeMillis() - startMs);
        return new AuthTokenResponse(token, saved.getId(), saved.getUsername());
    }
}
