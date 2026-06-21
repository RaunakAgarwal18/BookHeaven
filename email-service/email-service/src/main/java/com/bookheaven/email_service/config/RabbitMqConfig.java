package com.bookheaven.email_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import static com.bookheaven.email_service.constants.EmailConstant.*;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(ORDER_CONFIRMED_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue welcomeQueue() {
        return QueueBuilder.durable(WELCOME_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ)
                .build();
    }

    @Bean
    public Binding orderConfirmedBinding(Queue orderConfirmedQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(orderConfirmedQueue)
                .to(emailExchange)
                .with(ORDER_CONFIRMED_KEY);
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(paymentFailedQueue)
                .to(emailExchange)
                .with(PAYMENT_FAILED_KEY);
    }
    @Bean
    public Binding welcomeBinding(Queue welcomeQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(welcomeQueue)
                .to(emailExchange)
                .with(WELCOME_KEY);
    }


    @Bean
    public Queue sellerOrderQueue() {
        return QueueBuilder.durable(SELLER_ORDER_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding sellerOrderBinding(Queue sellerOrderQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(sellerOrderQueue)
                .to(emailExchange)
                .with(SELLER_ORDER_KEY);
    }

    @Bean
    public Queue orderShippedQueue() {
        return QueueBuilder.durable(ORDER_SHIPPED_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderShippedBinding(Queue orderShippedQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(orderShippedQueue)
                .to(emailExchange)
                .with(ORDER_SHIPPED_KEY);
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return QueueBuilder.durable(ORDER_DELIVERED_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderDeliveredBinding(Queue orderDeliveredQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(orderDeliveredQueue)
                .to(emailExchange)
                .with(ORDER_DELIVERED_KEY);
    }

    @Bean
    public Queue contactQueue() {
        return QueueBuilder.durable(CONTACT_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding contactBinding(Queue contactQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(contactQueue)
                .to(emailExchange)
                .with(CONTACT_KEY);
    }

    @Bean
    public Queue missingRazorpayQueue() {
        return new Queue(MISSING_RAZORPAY_QUEUE);
    }

    @Bean
    public Binding missingRazorpayBinding(Queue missingRazorpayQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(missingRazorpayQueue)
                .to(emailExchange)
                .with(MISSING_RAZORPAY_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 4000) // 1s → 2s → 4s
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();

        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }
}