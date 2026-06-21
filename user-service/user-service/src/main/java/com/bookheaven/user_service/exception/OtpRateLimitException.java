package com.bookheaven.user_service.exception;

public class OtpRateLimitException extends RuntimeException {
    public OtpRateLimitException(String message) { super(message); }
}