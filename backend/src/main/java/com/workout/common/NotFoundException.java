package com.workout.common;

/**
 * 资源不存在或对当前用户不可见，映射为 HTTP 404。
 */
public class NotFoundException extends RuntimeException {

    /**
     * 构造 404 业务异常。
     *
     * @param message 中文说明
     */
    public NotFoundException(String message) {
        super(message);
    }
}
