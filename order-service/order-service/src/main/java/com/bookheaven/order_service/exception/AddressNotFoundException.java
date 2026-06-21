package com.bookheaven.order_service.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String message) { super(message); }
}