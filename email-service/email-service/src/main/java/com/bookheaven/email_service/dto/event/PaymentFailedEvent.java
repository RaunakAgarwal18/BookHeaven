package com.bookheaven.email_service.dto.event;

import lombok.Data;

@Data
public class PaymentFailedEvent {
    private String to;
    private String username;
    private String orderId;
}