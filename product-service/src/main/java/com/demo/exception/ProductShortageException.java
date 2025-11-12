package com.demo.exception;

public class ProductShortageException extends RuntimeException {
    public ProductShortageException(String message) {
        super(message);
    }
}
