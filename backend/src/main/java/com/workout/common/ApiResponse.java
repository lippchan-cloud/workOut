package com.workout.common;

import java.time.Instant;
import java.util.UUID;

/**
 * Unified API envelope: { code, msg, data, requestId, timestamp }.
 */
public class ApiResponse<T> {

    private final int code;
    private final String msg;
    private final T data;
    private final String requestId;
    private final Instant timestamp;

    public ApiResponse(int code, String msg, T data, String requestId, Instant timestamp) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = timestamp;
    }

    /**
     * Build a successful envelope with generated request metadata.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "OK", data, UUID.randomUUID().toString(), Instant.now());
    }

    /**
     * Build a failed envelope with business/http-aligned code and message.
     */
    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<>(code, msg, null, UUID.randomUUID().toString(), Instant.now());
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
