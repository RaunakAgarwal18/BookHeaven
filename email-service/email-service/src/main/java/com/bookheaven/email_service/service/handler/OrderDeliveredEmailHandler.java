package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.email_service.dto.event.OrderDeliveredEvent;
import com.bookheaven.email_service.util.ItemRowBuilder;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveredEmailHandler implements EmailHandler<OrderDeliveredEvent> {

    private final EmailSenderUtil mailSender;

    @Override
    public Class<OrderDeliveredEvent> getEventType() {
        return OrderDeliveredEvent.class;
    }

    @Override
    public void handle(OrderDeliveredEvent event) {
        String subject = EmailConstant.ORDER_DELIVERED_MAIL_SUBJECT;
        String itemsHtml = ItemRowBuilder.buildDeliveredItemsHtml(event.getItems());
        String address = event.getShippingAddress() != null ? event.getShippingAddress() : "N/A";

        String mailBody = MailTemplate.ORDER_DELIVERED_MAIL
                .replace("{{USERNAME}}", event.getUsername())
                .replace("{{ORDER_ID}}", event.getOrderId())
                .replace("{{SHIPPING_ADDRESS}}", address)
                .replace("{{ITEMS_ROWS}}", itemsHtml);

        mailSender.safeSend(event.getTo(), subject, mailBody, "Error sending Order Delivered notification, try again later!");
    }
}
