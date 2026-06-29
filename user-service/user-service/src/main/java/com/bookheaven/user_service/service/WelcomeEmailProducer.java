package com.bookheaven.user_service.service;

import com.bookheaven.common.dto.event.WelcomeEmailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.bookheaven.user_service.constant.AppConstants;

@Service
@RequiredArgsConstructor
public class WelcomeEmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishWelcomeEmail(WelcomeEmailEvent event) {
        rabbitTemplate.convertAndSend(
                AppConstants.EXCHANGE_EMAIL,
                AppConstants.ROUTING_KEY_WELCOME,
                event
        );
    }
}
