package com.bookheaven.email_service.util;

import com.bookheaven.email_service.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderUtil {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void safeSend(String to, String subject, String body, String errorContext) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail, "BookHeaven");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error(errorContext, ex);
            throw new EmailSendingException(errorContext);
        }
    }

    public void safeSendWithAttachmentAndReplyTo(String to, String replyTo, String subject, String body, 
                                                String base64Attachment, String attachmentFilename, String errorContext) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail, "BookHeaven");
            helper.setTo(to);
            if (replyTo != null) {
                helper.setReplyTo(replyTo);
            }
            helper.setSubject(subject);
            helper.setText(body, true);

            if (base64Attachment != null && !base64Attachment.trim().isEmpty()) {
                String base64Data = base64Attachment;
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Data);
                String filename = attachmentFilename != null && !attachmentFilename.trim().isEmpty() ? attachmentFilename : "attachment.png";
                helper.addAttachment(filename, new ByteArrayResource(decodedBytes));
            }

            javaMailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error(errorContext, ex);
            throw new EmailSendingException(errorContext);
        }
    }
}
