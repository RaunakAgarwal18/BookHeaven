package com.bookheaven.order_service.exception;

public class PaymentServiceException extends RuntimeException {
    public PaymentServiceException(String message) { super(message); }
}