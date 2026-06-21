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
        String subject = event.isFinalDelivery() ? EmailConstant.ORDER_DELIVERED_MAIL_SUBJECT : "Partial Delivery for Order " + event.getOrderId();
        
        java.util.List<OrderDeliveredEvent.DeliveredItem> itemsToShow = event.getNewlyDeliveredItems() != null && !event.getNewlyDeliveredItems().isEmpty()
                ? event.getNewlyDeliveredItems() : event.getItems();
        String itemsHtml = ItemRowBuilder.buildDeliveredItemsHtml(itemsToShow);
        String address = event.getShippingAddress() != null ? event.getShippingAddress() : "N/A";

        String mailBody = MailTemplate.ORDER_DELIVERED_MAIL
                .replace("{{USERNAME}}", event.getUsername())
                .replace("{{ORDER_ID}}", event.getOrderId())
                .replace("{{SHIPPING_ADDRESS}}", address)
                .replace("{{ITEMS_ROWS}}", itemsHtml);

        mailSender.safeSend(event.getTo(), subject, mailBody, "Error sending Order Delivered notification, try again later!");
    }
}
