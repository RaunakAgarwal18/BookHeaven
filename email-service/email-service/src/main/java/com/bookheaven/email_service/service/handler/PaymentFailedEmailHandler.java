package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.email_service.dto.event.PaymentFailedEvent;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFailedEmailHandler implements EmailHandler<PaymentFailedEvent> {

    private final EmailSenderUtil mailSender;

    @Override
    public Class<PaymentFailedEvent> getEventType() {
        return PaymentFailedEvent.class;
    }

    @Override
    public void handle(PaymentFailedEvent event) {
        String subject = EmailConstant.PAYMENT_FAILED_MAIL_SUBJECT;
        String mailBody = MailTemplate.PAYMENT_FAILED_MAIL.replace("{{USERNAME}}", event.getUsername())
                .replace("{{ORDER_ID}}", event.getOrderId());
        mailSender.safeSend(event.getTo(), subject, mailBody, "Error Sending Payment Failed Mail, Try again later!!");
    }
}
