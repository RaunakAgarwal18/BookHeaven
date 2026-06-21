package com.bookheaven.user_service.exception;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) { super(message); }
}