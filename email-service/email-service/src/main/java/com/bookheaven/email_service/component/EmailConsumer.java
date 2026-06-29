package com.bookheaven.email_service.component;

import com.bookheaven.common.dto.event.OrderConfirmedEvent;
import com.bookheaven.common.dto.event.OrderShippedEvent;
import com.bookheaven.common.dto.event.OrderDeliveredEvent;
import com.bookheaven.common.dto.event.PaymentFailedEvent;
import com.bookheaven.common.dto.event.WelcomeEmailEvent;
import com.bookheaven.email_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.bookheaven.email_service.constants.EmailConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;
    private final ProcessedMessageTracker tracker;

    @RabbitListener(queues = ORDER_CONFIRMED_QUEUE)
    public void consumeOrderConfirmed(OrderConfirmedEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate order-confirmed message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }

    @RabbitListener(queues = PAYMENT_FAILED_QUEUE)
    public void consumePaymentFailed(PaymentFailedEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate payment-failed message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }

    @RabbitListener(queues = WELCOME_QUEUE)
    public void consumeWelcomeEmail(WelcomeEmailEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate welcome message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }

    @RabbitListener(queues = SELLER_ORDER_QUEUE)
    public void consumeSellerOrderNotification(
            com.bookheaven.common.dto.event.SellerOrderEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate seller-order message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }

    @RabbitListener(queues = ORDER_SHIPPED_QUEUE)
    public void consumeOrderShipped(OrderShippedEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate order-shipped message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }

    @RabbitListener(queues = ORDER_DELIVERED_QUEUE)
    public void consumeOrderDelivered(OrderDeliveredEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate order-delivered message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }

    @RabbitListener(queues = CONTACT_QUEUE)
    public void consumeContactEmail(com.bookheaven.common.dto.event.ContactEmailEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate contact message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }

    @RabbitListener(queues = MISSING_RAZORPAY_QUEUE)
    public void consumeMissingRazorpayIdEmail(com.bookheaven.common.dto.event.MissingRazorpayIdEvent event, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (tracker.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate missing-razorpay message [{}] — skipping to prevent duplicate email", messageId);
            return;
        }
        emailService.processEmailEvent(event);
        tracker.markProcessed(messageId);
    }
}
