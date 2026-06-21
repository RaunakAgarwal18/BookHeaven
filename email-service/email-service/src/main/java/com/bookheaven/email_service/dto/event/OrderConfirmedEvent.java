package com.bookheaven.email_service.dto.event;

import lombok.Data;

@Data
public class OrderConfirmedEvent {
    private String to;
    private String username;
    private String orderId;
    private String shippingAddress;
}