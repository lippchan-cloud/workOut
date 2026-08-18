package com.workout.common;

/**
 * 已认证但权限不足，映射为 HTTP 403。
 */
public class ForbiddenException extends RuntimeException {

    /**
     * 构造禁止访问异常。
     *
     * @param message 中文说明
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
