package com.workout.config;

import com.workout.modules.auth.infrastructure.JwtAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全与密码编码配置（配置层）。
 * 注册/登录与健康检查放行；其余 /api/v1/** 必须携带有效 Bearer JWT。
 * CMS 账户列表不再公开放行，须 authenticated + 业务层 ADMIN 校验。
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, AdminProperties.class, WorkoutPublicProperties.class})
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 提供 BCrypt 密码编码器供注册/登录使用。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("[安全配置] passwordEncoder start algorithm=BCrypt");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        log.info("[安全配置] passwordEncoder done");
        return encoder;
    }

    /**
     * 配置无状态 JWT 链路：auth/health 公开，业务 API（含 CMS）需认证，SPA 路由放行。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthFilter jwtAuthFilter, JsonAuthEntryPoint jsonAuthEntryPoint) throws Exception {
        log.info("[安全配置] securityFilterChain start statelessJwt=true");
        // API 使用 Bearer Token，关闭 CSRF 与 Session
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/health")
                        .permitAll()
                        .requestMatchers("/api/v1/reports/**")
                        .permitAll()
                        .requestMatchers("/api/v1/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(handling -> handling.authenticationEntryPoint(jsonAuthEntryPoint))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        SecurityFilterChain chain = http.build();
        log.info("[安全配置] securityFilterChain done public=/api/v1/auth/register,/api/v1/auth/login,/api/v1/health,/api/v1/reports/**");
        return chain;
    }
}
