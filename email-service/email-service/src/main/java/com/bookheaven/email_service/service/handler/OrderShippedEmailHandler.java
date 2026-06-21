package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.email_service.dto.event.OrderShippedEvent;
import com.bookheaven.email_service.util.ItemRowBuilder;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderShippedEmailHandler implements EmailHandler<OrderShippedEvent> {

    private final EmailSenderUtil mailSender;
    private final com.bookheaven.email_service.util.PdfInvoiceGenerator pdfInvoiceGenerator;

    @Override
    public Class<OrderShippedEvent> getEventType() {
        return OrderShippedEvent.class;
    }

    @Override
    public void handle(OrderShippedEvent event) {
        String subject = EmailConstant.ORDER_SHIPPED_MAIL_SUBJECT;
        String itemsHtml = ItemRowBuilder.buildShippedItemsHtml(event.getItems());
        String address = event.getShippingAddress() != null ? event.getShippingAddress() : "N/A";

        String mailBody = MailTemplate.ORDER_SHIPPED_MAIL
                .replace("{{USERNAME}}", event.getUsername())
                .replace("{{ORDER_ID}}", event.getOrderId())
                .replace("{{SHIPPING_ADDRESS}}", address)
                .replace("{{ITEMS_ROWS}}", itemsHtml);

        String base64Pdf = pdfInvoiceGenerator.generateInvoicePdfBase64(event);
        String filename = "Invoice_" + event.getOrderId() + ".pdf";

        if (base64Pdf != null) {
            mailSender.safeSendWithAttachmentAndReplyTo(event.getTo(), null, subject, mailBody, base64Pdf, filename, "Error sending Order Shipped notification, try again later!");
        } else {
            mailSender.safeSend(event.getTo(), subject, mailBody, "Error sending Order Shipped notification, try again later!");
        }
    }
}
