package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.common.dto.request.PasswordResetRequest;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetEmailHandler implements EmailHandler<PasswordResetRequest> {

    private final EmailSenderUtil mailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public Class<PasswordResetRequest> getEventType() {
        return PasswordResetRequest.class;
    }

    @Override
    public void handle(PasswordResetRequest request) {
        String resetLink = frontendUrl + "/reset-password?token=" + request.getToken();
        String subject = EmailConstant.PASSWORD_RESET_MAIL_SUBJECT;
        String mailBody = MailTemplate.PASSWORD_RESET_MAIL.replace("{{RESET_LINK}}", resetLink);
        mailSender.safeSend(request.getTo(), subject, mailBody, "Error Sending Password Reset Link, Try again later!!");
    }
}
