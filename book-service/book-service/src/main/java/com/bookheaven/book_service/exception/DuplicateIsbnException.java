package com.bookheaven.book_service.exception;

public class DuplicateIsbnException extends RuntimeException {
    public DuplicateIsbnException(String message) { super(message); }
}