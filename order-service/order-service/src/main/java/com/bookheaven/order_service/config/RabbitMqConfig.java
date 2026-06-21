package com.bookheaven.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.bookheaven.order_service.constants.RabbitMqConstant.*;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange orderTimeoutExchange() {
        return new TopicExchange(ORDER_TIMEOUT_EXCHANGE);
    }

    @Bean
    public TopicExchange ledgerExchange() {
        return new TopicExchange(LEDGER_EXCHANGE);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE)
                .ttl(1800000)
                .deadLetterExchange(ORDER_TIMEOUT_EXCHANGE)
                .deadLetterRoutingKey(ORDER_TIMEOUT_PROCESSING_KEY)
                .build();
    }

    @Bean
    public Queue orderTimeoutProcessingQueue() {
        return QueueBuilder
                .durable(ORDER_TIMEOUT_PROCESSING_QUEUE)
                .build();
    }

    @Bean
    public Binding orderTimeoutBinding(Queue orderTimeoutQueue, TopicExchange orderTimeoutExchange) {
        return BindingBuilder
                .bind(orderTimeoutQueue)
                .to(orderTimeoutExchange)
                .with(ORDER_TIMEOUT_KEY);
    }

    @Bean
    public Binding orderTimeoutProcessingBinding(Queue orderTimeoutProcessingQueue, TopicExchange orderTimeoutExchange) {
        return BindingBuilder.bind(orderTimeoutProcessingQueue)
                .to(orderTimeoutExchange)
                .with(ORDER_TIMEOUT_PROCESSING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}