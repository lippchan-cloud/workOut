package com.workout.modules.auth.application;

import com.workout.common.BusinessException;
import com.workout.modules.auth.api.AuthTokenResponse;
import com.workout.modules.auth.infrastructure.JwtService;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务（应用层）。
 * 负责注册与登录编排：唯一性校验、密码哈希核对、落库、签发 JWT；不暴露仓储细节给 Controller。
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

    /**
     * 校验用户名与密码并签发 Token。
     * 用户不存在与密码错误使用同一文案，避免泄露账号是否已注册。
     *
     * @param username 登录名
     * @param rawPassword 明文密码（不会落库）
     * @return token 与用户标识
     */
    public AuthTokenResponse login(String username, String rawPassword) {
        long startMs = System.currentTimeMillis();
        // 关键入口：只记录用户名，禁止打印明文密码
        log.info("[鉴权登录] login start username={}", username);
        // 按用户名加载用户；找不到与密码错误走同一失败文案
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.error("[鉴权登录] login failed code=400 msg=用户名或密码错误 username={}", username);
            throw new BusinessException("用户名或密码错误");
        }
        // 关键实体：核对通过后的用户标识
        log.info("[鉴权登录] loaded entityType=UserEntity id={}, username={}", user.getId(), user.getUsername());
        // 登录成功签发 JWT
        String token = jwtService.issueToken(user.getId(), user.getUsername());
        log.info(
                "[鉴权登录] login done success=true userId={}, elapsedMs={}",
                user.getId(),
                System.currentTimeMillis() - startMs);
        return new AuthTokenResponse(token, user.getId(), user.getUsername());
    }
}
