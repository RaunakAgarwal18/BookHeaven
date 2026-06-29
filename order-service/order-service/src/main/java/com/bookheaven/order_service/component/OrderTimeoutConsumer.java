package com.bookheaven.order_service.component;

import com.bookheaven.common.dto.event.OrderTimeoutEvent;
import com.bookheaven.common.dto.event.PaymentFailedEvent;
import com.bookheaven.order_service.entity.Order;
import com.bookheaven.order_service.exception.OrderNotFoundException;
import com.bookheaven.order_service.service.impl.OrderEventProducer;
import com.bookheaven.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.bookheaven.common.constant.RabbitMqConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderService orderService;
    private final OrderEventProducer orderEventProducer;

    @RabbitListener(queues = ORDER_TIMEOUT_PROCESSING_QUEUE)
    public void processTimeout(OrderTimeoutEvent event) {
        Order order;
        try {
            order = orderService.getOrderById(event.getOrderId());
        } catch (OrderNotFoundException e) {
            log.warn("Order {} not found during timeout processing. It may have been rolled back or deleted. Ignoring timeout.", event.getOrderId());
            return;
        }
        if (order.getStatus() == Order.OrderStatus.PENDING) {
            // Payment never completed — cancel the order
            orderService.failOrder(order.getId(), "Payment timeout after 15 minutes");

            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent();
            paymentFailedEvent.setTo(order.getEmail());
            paymentFailedEvent.setUsername(order.getUsername());
            paymentFailedEvent.setOrderId(order.getOrderReference());
            orderEventProducer.publishPaymentFailedEvent(paymentFailedEvent);
            log.info("Order {} timed out and marked as FAILED. Payment failed email sent.", order.getId());
        } else {
            // Order was already CONFIRMED, CANCELLED, FAILED etc. — nothing to do.
            log.info("Order {} timeout fired but status is already {}. No action taken.", order.getId(), order.getStatus());
        }
    }
}
