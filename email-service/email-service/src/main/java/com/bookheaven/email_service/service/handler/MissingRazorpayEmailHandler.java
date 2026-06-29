package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.common.dto.event.MissingRazorpayIdEvent;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissingRazorpayEmailHandler implements EmailHandler<MissingRazorpayIdEvent> {

    private final EmailSenderUtil mailSender;

    @Override
    public Class<MissingRazorpayIdEvent> getEventType() {
        return MissingRazorpayIdEvent.class;
    }

    @Override
    public void handle(MissingRazorpayIdEvent event) {
        String subject = EmailConstant.MISSING_RAZORPAY_SUBJECT;
        String mailBody = MailTemplate.MISSING_RAZORPAY_MAIL
                .replace("{{USERNAME}}", event.getUsername())
                .replace("{{CURRENCY}}", event.getCurrency())
                .replace("{{AMOUNT}}", String.format("%.2f", event.getAmountPending()));

        mailSender.safeSend(event.getEmail(), subject, mailBody, "Error sending Missing Razorpay ID Email, try again later!");
    }
}
