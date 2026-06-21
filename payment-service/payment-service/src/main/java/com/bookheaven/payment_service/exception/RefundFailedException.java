package com.bookheaven.payment_service.exception;

public class RefundFailedException extends RuntimeException {
    public RefundFailedException(String message) { super(message); }
}