package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.constant.AppConstants;
import com.bookheaven.user_service.dto.event.ContactEmailEvent;
import com.bookheaven.user_service.dto.requestDto.ContactRequest;
import com.bookheaven.user_service.service.ContactFormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactFormServiceImpl implements ContactFormService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void submitContactForm(ContactRequest request) {
        log.info("Contact form submitted by email - {}", request.getEmail());
        ContactEmailEvent event = ContactEmailEvent.builder()
                .email(request.getEmail())
                .subject(request.getSubject())
                .description(request.getDescription())
                .screenshotBase64(request.getScreenshotBase64())
                .filename(request.getFilename())
                .build();
        rabbitTemplate.convertAndSend(AppConstants.EXCHANGE_EMAIL, AppConstants.ROUTING_KEY_CONTACT, event);
        log.info("Contact email event published to RabbitMQ.");
    }
}
