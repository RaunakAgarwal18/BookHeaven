package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.email_service.dto.event.OrderConfirmedEvent;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConfirmedEmailHandler implements EmailHandler<OrderConfirmedEvent> {

    private final EmailSenderUtil mailSender;

    @Override
    public Class<OrderConfirmedEvent> getEventType() {
        return OrderConfirmedEvent.class;
    }

    @Override
    public void handle(OrderConfirmedEvent event) {
        String subject = EmailConstant.ORDER_CONFIRM_MAIL_SUBJECT;
        String address = event.getShippingAddress() != null ? event.getShippingAddress() : "N/A";
        String mailBody = MailTemplate.ORDER_CONFIRMED_MAIL.replace("{{USERNAME}}", event.getUsername())
                .replace("{{ORDER_ID}}", event.getOrderId())
                .replace("{{SHIPPING_ADDRESS}}", address);
        mailSender.safeSend(event.getTo(), subject, mailBody, "Error Sending Order Confirmation Mail, Try again later!!");
    }
}
