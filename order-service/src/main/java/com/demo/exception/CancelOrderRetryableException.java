package com.demo.exception;

public class CancelOrderRetryableException extends RuntimeException {
    public CancelOrderRetryableException(String message) {
        super(message);
    }
}
