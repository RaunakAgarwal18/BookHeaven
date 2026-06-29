package com.bookheaven.common.dto.event;

import lombok.Data;

@Data
public class PaymentFailedEvent {
    private String to;
    private String username;
    private String orderId;
}
