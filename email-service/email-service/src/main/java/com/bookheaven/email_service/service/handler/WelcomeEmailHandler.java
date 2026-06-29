package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.common.dto.event.WelcomeEmailEvent;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WelcomeEmailHandler implements EmailHandler<WelcomeEmailEvent> {

    private final EmailSenderUtil mailSender;

    @Override
    public Class<WelcomeEmailEvent> getEventType() {
        return WelcomeEmailEvent.class;
    }

    @Override
    public void handle(WelcomeEmailEvent event) {
        String subject = EmailConstant.WELCOME_MAIL_SUBJECT;
        String template = MailTemplate.WELCOME_MAIL;

        if (event.getRole() != null && event.getRole().equalsIgnoreCase("SELLER")) {
            template = MailTemplate.WELCOME_SELLER_MAIL;
            subject = "BookHeaven - Welcome to the Merchant Hub!";
        }

        String mailBody = template.replace("{{USERNAME}}", event.getUsername());
        mailSender.safeSend(event.getTo(), subject, mailBody, "Error Sending Welcome Mail, Try again later!!");
    }
}
