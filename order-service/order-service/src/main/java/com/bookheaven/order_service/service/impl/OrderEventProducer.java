package com.bookheaven.order_service.service.impl;

import com.bookheaven.order_service.dto.Event.OrderConfirmedEvent;
import com.bookheaven.order_service.dto.Event.OrderLedgerEvent;
import com.bookheaven.order_service.dto.Event.OrderShippedEvent;
import com.bookheaven.order_service.dto.Event.OrderDeliveredEvent;
import com.bookheaven.order_service.dto.Event.OrderTimeoutEvent;
import com.bookheaven.order_service.dto.Event.PaymentFailedEvent;
import com.bookheaven.order_service.dto.Event.SellerOrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.bookheaven.order_service.constants.RabbitMqConstant.*;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderTimeoutEvent(OrderTimeoutEvent event) {
        rabbitTemplate.convertAndSend(ORDER_TIMEOUT_EXCHANGE, ORDER_TIMEOUT_KEY, event);
    }

    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, PAYMENT_FAILED_KEY, event, msg -> {
            // Deterministic ID: same orderId always produces the same messageId.
            // Email consumer uses this to skip redeliveries of the same event.
            msg.getMessageProperties().setMessageId("payment-failed-" + event.getOrderId());
            return msg;
        });
    }

    public void publishOrderConfirmedEvent(OrderConfirmedEvent event) {
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, ORDER_CONFIRMED_KEY, event, msg -> {
            msg.getMessageProperties().setMessageId("order-confirmed-" + event.getOrderId());
            return msg;
        });
    }

    public void publishSellerOrderEvent(SellerOrderEvent event) {
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, SELLER_ORDER_KEY, event, msg -> {
            // Unique per seller per order (one order can have items from multiple sellers)
            msg.getMessageProperties().setMessageId(
                    "seller-order-" + event.getOrderId() + "-" + event.getTo());
            return msg;
        });
    }

    public void publishOrderShippedEvent(OrderShippedEvent event) {
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, ORDER_SHIPPED_KEY, event, msg -> {
            msg.getMessageProperties().setMessageId("order-shipped-" + event.getOrderId());
            return msg;
        });
    }

    public void publishOrderDeliveredEvent(OrderDeliveredEvent event) {
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, ORDER_DELIVERED_KEY, event, msg -> {
            msg.getMessageProperties().setMessageId("order-delivered-" + event.getOrderId());
            return msg;
        });
    }

    public void publishOrderLedgerEvent(OrderLedgerEvent event) {
        rabbitTemplate.convertAndSend(LEDGER_EXCHANGE, LEDGER_KEY, event);
    }
}