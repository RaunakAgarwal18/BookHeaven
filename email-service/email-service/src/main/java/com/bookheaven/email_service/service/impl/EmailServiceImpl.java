package com.bookheaven.email_service.service.impl;

import com.bookheaven.email_service.service.EmailService;
import com.bookheaven.email_service.service.handler.EmailHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final Map<Class<?>, EmailHandler<?>> handlerRegistry;

    public EmailServiceImpl(List<EmailHandler<?>> handlers) {
        this.handlerRegistry = handlers.stream()
                .collect(Collectors.toMap(
                        EmailHandler::getEventType,
                        handler -> handler
                ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void processEmailEvent(Object event) {
        EmailHandler<Object> handler = (EmailHandler<Object>) handlerRegistry.get(event.getClass());
        if (handler != null) {
            handler.handle(event);
        } else {
            throw new IllegalArgumentException("No handler found for event type: " + event.getClass());
        }
    }
}
