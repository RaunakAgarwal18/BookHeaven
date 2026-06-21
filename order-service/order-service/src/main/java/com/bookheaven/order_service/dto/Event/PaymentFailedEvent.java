package com.bookheaven.order_service.dto.Event;

import lombok.Data;

@Data
public class PaymentFailedEvent {
    private String to;
    private String username;
    private String orderId;
}
