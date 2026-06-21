package com.bookheaven.user_service.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bookheaven.user_service.constant.AppConstants;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(AppConstants.EXCHANGE_EMAIL);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}