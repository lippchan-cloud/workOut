package com.workout.modules.auth.api;

import com.workout.common.ApiRequest;
import com.workout.common.ApiResponse;
import com.workout.modules.auth.application.AuthService;
import com.workout.modules.auth.domain.AuthPrincipal;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 HTTP 入口（接口层）。
 * 仅做参数接收与响应包装，业务规则下沉到 AuthService。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * 注入认证应用服务。
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册：校验通过后返回 JWT。
     */
    @PostMapping("/register")
    public ApiResponse<AuthTokenResponse> register(@Valid @RequestBody ApiRequest<RegisterRequest> body) {
        RegisterRequest request = body.getRequest();
        // 关键入口：记录用户名，不记录密码
        log.info("[鉴权注册] AuthController.register start username={}", request.getUsername());
        // 委托应用服务完成注册与发 Token
        AuthTokenResponse data = authService.register(request.getUsername(), request.getPassword());
        // 关键实体：响应中的用户标识
        log.info("[鉴权注册] AuthController.register done userId={}, username={}", data.getUserId(), data.getUsername());
        return ApiResponse.ok(data);
    }

    /**
     * 用户登录：校验通过后返回 JWT。
     */
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody ApiRequest<LoginRequest> body) {
        LoginRequest request = body.getRequest();
        // 关键入口：只记录用户名，不记录密码
        log.info("[鉴权登录] AuthController.login start username={}", request.getUsername());
        // 委托应用服务核对哈希并签发 Token
        AuthTokenResponse data = authService.login(request.getUsername(), request.getPassword());
        // 关键实体：登录成功后的用户标识
        log.info("[鉴权登录] AuthController.login done userId={}, username={}", data.getUserId(), data.getUsername());
        return ApiResponse.ok(data);
    }

    /**
     * 修改当前用户密码。
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ApiRequest<ChangePasswordRequest> body) {
        AuthPrincipal principal = CurrentUser.require();
        ChangePasswordRequest request = body.getRequest();
        log.info("[鉴权改密] AuthController.changePassword start userId={}", principal.getUserId());
        authService.changePassword(principal.getUserId(), request.getCurrentPassword(), request.getNewPassword());
        log.info("[鉴权改密] AuthController.changePassword done userId={}", principal.getUserId());
        return ApiResponse.ok(null);
    }

    /**
     * 注销当前账号并删除本人数据。
     */
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMe() {
        AuthPrincipal principal = CurrentUser.require();
        log.info("[鉴权注销] AuthController.deleteMe start userId={}", principal.getUserId());
        authService.deleteMe(principal.getUserId());
        log.info("[鉴权注销] AuthController.deleteMe done userId={}", principal.getUserId());
        return ApiResponse.ok(null);
    }
}
