package com.bookheaven.payment_service.exception;

public class InvalidWebhookException extends RuntimeException {
    public InvalidWebhookException(String message) { super(message); }
}