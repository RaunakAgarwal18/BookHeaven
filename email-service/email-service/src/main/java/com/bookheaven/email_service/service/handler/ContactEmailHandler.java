package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.dto.event.ContactEmailEvent;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContactEmailHandler implements EmailHandler<ContactEmailEvent> {

    private final EmailSenderUtil mailSender;

    @Value("${store.admin.email:admin@bookstore.com}")
    private String storeAdminEmail;

    @Override
    public Class<ContactEmailEvent> getEventType() {
        return ContactEmailEvent.class;
    }

    @Override
    public void handle(ContactEmailEvent event) {
        String subject = EmailConstant.CONTACT_MAIL_SUBJECT + " - " + event.getSubject();
        String mailBody = "<strong>From:</strong> " + event.getEmail() + "<br/><br/>" +
                "<strong>Message:</strong><br/>" + event.getDescription().replace("\n", "<br/>");

        mailSender.safeSendWithAttachmentAndReplyTo(
                storeAdminEmail,
                event.getEmail(),
                subject,
                mailBody,
                event.getScreenshotBase64(),
                event.getFilename(),
                "Error sending Contact Email, try again later!"
        );
    }
}
