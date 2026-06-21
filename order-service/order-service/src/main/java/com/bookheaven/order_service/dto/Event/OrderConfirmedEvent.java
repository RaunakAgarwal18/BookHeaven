package com.bookheaven.order_service.dto.Event;

import lombok.Data;

@Data
public class OrderConfirmedEvent {
    private String to;
    private String username;
    private String orderId;
    private String shippingAddress;
}