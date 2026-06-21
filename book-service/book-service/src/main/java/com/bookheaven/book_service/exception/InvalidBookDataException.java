package com.bookheaven.book_service.exception;

public class InvalidBookDataException extends RuntimeException {
    public InvalidBookDataException(String message) { super(message); }
}