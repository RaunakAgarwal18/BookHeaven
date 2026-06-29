package com.bookheaven.order_service.exception;

public class OrderCancellationWindowException extends RuntimeException {
    public OrderCancellationWindowException(String message) {
        super(message);
    }
}
