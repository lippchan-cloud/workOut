package com.workout.modules.auth.domain;

/**
 * 邮箱验证码用途（领域枚举）。
 * 仅 BIND / UNBIND / LOGIN，不引入找回密码等其它通道。
 */
public enum EmailCodePurpose {
    BIND,
    UNBIND,
    LOGIN
}
