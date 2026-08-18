package com.workout.common;

/**
 * Domain/business rule violation mapped to HTTP 400.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
