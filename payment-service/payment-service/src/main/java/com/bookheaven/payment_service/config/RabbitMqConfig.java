package com.bookheaven.payment_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String LEDGER_EXCHANGE = "ledger.exchange";
    public static final String SELLER_LEDGER_QUEUE = "seller-ledger-queue";
    public static final String LEDGER_KEY = "ledger.order.confirmed";

    public static final String SELLER_PAYOUT_EXCHANGE = "seller-payout-exchange";
    public static final String SELLER_PAYOUT_QUEUE = "seller-payout-queue";
    public static final String PAYOUT_ROUTING_KEY = "payout.routing.key";

    @Bean
    public TopicExchange ledgerExchange() {
        return new TopicExchange(LEDGER_EXCHANGE);
    }

    @Bean
    public Queue sellerLedgerQueue() {
        return QueueBuilder.durable(SELLER_LEDGER_QUEUE).build();
    }

    @Bean
    public Binding bindingLedger(Queue sellerLedgerQueue, TopicExchange ledgerExchange) {
        return BindingBuilder.bind(sellerLedgerQueue).to(ledgerExchange).with(LEDGER_KEY);
    }

    @Bean
    public TopicExchange sellerPayoutExchange() {
        return new TopicExchange(SELLER_PAYOUT_EXCHANGE);
    }

    @Bean
    public Queue sellerPayoutQueue() {
        return QueueBuilder.durable(SELLER_PAYOUT_QUEUE).build();
    }

    @Bean
    public Binding bindingPayout(Queue sellerPayoutQueue, TopicExchange sellerPayoutExchange) {
        return BindingBuilder.bind(sellerPayoutQueue).to(sellerPayoutExchange).with(PAYOUT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
