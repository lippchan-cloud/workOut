package com.workout.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 统一 POST 外层包装：业务字段放在 request 内（API 边界）。
 *
 * @param <T> 内层业务请求类型
 */
public class ApiRequest<T> {

    @NotNull(message = "request 不能为空")
    @Valid
    private T request;

    /**
     * 读取内层业务请求。
     */
    public T getRequest() {
        return request;
    }

    /**
     * 写入内层业务请求。
     */
    public void setRequest(T request) {
        this.request = request;
    }
}
