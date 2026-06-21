package com.bookheaven.order_service.exception;

public class OrderCancellationException extends RuntimeException {
    public OrderCancellationException(String message) { super(message); }
}