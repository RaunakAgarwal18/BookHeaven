package com.bookheaven.review_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String RATING_UPDATE_QUEUE = "book.rating.update.queue";
    public static final String RATING_UPDATE_EXCHANGE = "book.rating.update.exchange";
    public static final String RATING_UPDATE_ROUTING_KEY = "book.rating.update.routing.key";

    @Bean
    public Queue ratingUpdateQueue() {
        return new Queue(RATING_UPDATE_QUEUE, true);
    }

    @Bean
    public DirectExchange ratingUpdateExchange() {
        return new DirectExchange(RATING_UPDATE_EXCHANGE);
    }

    @Bean
    public Binding ratingUpdateBinding(Queue ratingUpdateQueue, DirectExchange ratingUpdateExchange) {
        return BindingBuilder.bind(ratingUpdateQueue).to(ratingUpdateExchange).with(RATING_UPDATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
