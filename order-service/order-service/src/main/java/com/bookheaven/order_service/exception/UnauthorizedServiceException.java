package com.bookheaven.order_service.exception;

public class UnauthorizedServiceException extends RuntimeException {
    public UnauthorizedServiceException(String message) {
        super(message);
    }
}
