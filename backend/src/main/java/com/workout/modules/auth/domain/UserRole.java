package com.workout.modules.auth.domain;

/**
 * 用户角色（领域枚举）。
 * 仅 USER / ADMIN 两档，不引入权限矩阵或组织角色。
 */
public enum UserRole {
    USER,
    ADMIN
}
