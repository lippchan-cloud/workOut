package com.workout.config;

import com.workout.modules.auth.infrastructure.JwtAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * TEMPORARY：第一阶段额外放行 GET /api/v1/admin/accounts（CMS 账户列表），后续必须加鉴权。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
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
     * 配置无状态 JWT 链路：auth/health 公开，业务 API 需认证，SPA 路由放行。
     * TEMPORARY：仅 GET /api/v1/admin/accounts 额外公开，不得扩大到其它业务 API。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthFilter jwtAuthFilter, JsonAuthEntryPoint jsonAuthEntryPoint) throws Exception {
        log.info("[安全配置] securityFilterChain start statelessJwt=true");
        // API 使用 Bearer Token，关闭 CSRF 与 Session
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/api/v1/health").permitAll()
                        // TEMPORARY：CMS 账户列表第一阶段无密码；后续删除本行并加管理员鉴权
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/accounts").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(handling -> handling.authenticationEntryPoint(jsonAuthEntryPoint))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        SecurityFilterChain chain = http.build();
        log.info(
                "[安全配置] securityFilterChain done public=/api/v1/auth/**,/api/v1/health,GET /api/v1/admin/accounts(TEMPORARY)");
        return chain;
    }
}
