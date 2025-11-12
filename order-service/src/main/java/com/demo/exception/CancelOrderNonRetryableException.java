package com.demo.exception;

public class CancelOrderNonRetryableException extends RuntimeException {
    public CancelOrderNonRetryableException(String message) {
        super(message);
    }
}
