package com.bookheaven.order_service.service.impl;

import com.bookheaven.common.dto.event.OrderConfirmedEvent;
import com.bookheaven.common.dto.event.OrderLedgerEvent;
import com.bookheaven.common.dto.event.OrderShippedEvent;
import com.bookheaven.common.dto.event.OrderDeliveredEvent;
import com.bookheaven.common.dto.event.OrderTimeoutEvent;
import com.bookheaven.common.dto.event.PaymentFailedEvent;
import com.bookheaven.common.dto.event.SellerOrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.bookheaven.common.constant.RabbitMqConstant.*;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Helper to prevent "Phantom Events" by deferring RabbitMQ publish until 
     * the current database transaction successfully commits.
     */
    private void sendAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            // Graceful fallback if called outside of a @Transactional block
            action.run();
        }
    }

    public void publishOrderTimeoutEvent(OrderTimeoutEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(ORDER_TIMEOUT_EXCHANGE, ORDER_TIMEOUT_KEY, event));
    }

    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, PAYMENT_FAILED_KEY, event, msg -> {
            // Deterministic ID: same orderId always produces the same messageId.
            // Email consumer uses this to skip redeliveries of the same event.
            msg.getMessageProperties().setMessageId("payment-failed-" + event.getOrderId());
            return msg;
        }));
    }

    public void publishOrderConfirmedEvent(OrderConfirmedEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, ORDER_CONFIRMED_KEY, event, msg -> {
            msg.getMessageProperties().setMessageId("order-confirmed-" + event.getOrderId());
            return msg;
        }));
    }

    public void publishSellerOrderEvent(SellerOrderEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, SELLER_ORDER_KEY, event, msg -> {
            // Unique per seller per order (one order can have items from multiple sellers)
            msg.getMessageProperties().setMessageId(
                    "seller-order-" + event.getOrderId() + "-" + event.getTo());
            return msg;
        }));
    }

    public void publishOrderShippedEvent(OrderShippedEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, ORDER_SHIPPED_KEY, event, msg -> {
            msg.getMessageProperties().setMessageId("order-shipped-" + event.getOrderId());
            return msg;
        }));
    }

    public void publishOrderDeliveredEvent(OrderDeliveredEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, ORDER_DELIVERED_KEY, event, msg -> {
            msg.getMessageProperties().setMessageId("order-delivered-" + event.getOrderId());
            return msg;
        }));
    }

    public void publishOrderLedgerEvent(OrderLedgerEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(LEDGER_EXCHANGE, LEDGER_KEY, event));
    }

    public void publishRefundEvent(com.bookheaven.common.dto.event.RefundEvent event) {
        sendAfterCommit(() -> rabbitTemplate.convertAndSend(REFUND_EXCHANGE, REFUND_DELAY_KEY, event));
    }
}
