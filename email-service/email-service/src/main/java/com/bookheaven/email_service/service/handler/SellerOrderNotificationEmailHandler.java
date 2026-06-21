package com.bookheaven.email_service.service.handler;

import com.bookheaven.email_service.constants.EmailConstant;
import com.bookheaven.email_service.constants.MailTemplate;
import com.bookheaven.email_service.dto.event.SellerOrderEvent;
import com.bookheaven.email_service.util.ItemRowBuilder;
import com.bookheaven.email_service.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerOrderNotificationEmailHandler implements EmailHandler<SellerOrderEvent> {

    private final EmailSenderUtil mailSender;

    @Override
    public Class<SellerOrderEvent> getEventType() {
        return SellerOrderEvent.class;
    }

    @Override
    public void handle(SellerOrderEvent event) {
        String subject = EmailConstant.SELLER_ORDER_MAIL_SUBJECT;
        double[] totalHolder = new double[1];
        String itemsHtml = ItemRowBuilder.buildSellerItemsHtml(event.getItems(), event.getCurrency(), totalHolder);

        String mailBody = MailTemplate.SELLER_ORDER_NOTIFICATION_MAIL
                .replace("{{SELLER_USERNAME}}", event.getSellerUsername())
                .replace("{{ORDER_ID}}", event.getOrderId())
                .replace("{{ITEMS_ROWS}}", itemsHtml)
                .replace("{{CURRENCY}}", event.getCurrency())
                .replace("{{TOTAL_AMOUNT}}", String.format("%.2f", totalHolder[0]))
                .replace("{{BUYER_USERNAME}}", event.getBuyerUsername())
                .replace("{{SHIPPING_ADDRESS}}", event.getShippingAddress() != null ? event.getShippingAddress() : "N/A");

        mailSender.safeSend(event.getTo(), subject, mailBody, "Error Sending Seller Order Notification Mail, Try again later!!");
    }
}
