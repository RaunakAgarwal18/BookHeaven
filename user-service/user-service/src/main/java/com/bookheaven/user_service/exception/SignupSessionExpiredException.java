package com.bookheaven.user_service.exception;

public class SignupSessionExpiredException extends RuntimeException {
    public SignupSessionExpiredException(String message) { super(message); }
}
