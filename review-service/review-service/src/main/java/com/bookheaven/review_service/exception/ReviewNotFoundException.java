package com.bookheaven.review_service.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) { super(message); }
}
