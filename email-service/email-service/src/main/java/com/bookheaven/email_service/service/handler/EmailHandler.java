package com.bookheaven.email_service.service.handler;

public interface EmailHandler<T> {
    Class<T> getEventType();
    void handle(T event);
}
