package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.email_service.dto.request.SendOtpRequest;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpEmailHandler implements EmailHandler<SendOtpRequest> {

    private final EmailSenderUtil mailSender;

    @Override
    public Class<SendOtpRequest> getEventType() {
        return SendOtpRequest.class;
    }

    @Override
    public void handle(SendOtpRequest request) {
        String subject = EmailConstant.OTP_MAIL_SUBJECT;
        String mailBody = MailTemplate.OTP_MAIL.replace("{{OTP}}", request.getOtp());
        mailSender.safeSend(request.getTo(), subject, mailBody, "Error Sending Otp, Try again later!!");
    }
}
