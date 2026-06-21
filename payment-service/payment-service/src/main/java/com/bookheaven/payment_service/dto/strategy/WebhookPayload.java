package com.bookheaven.payment_service.dto.strategy;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebhookPayload {
    private String event;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String paymentMethod;
    private String failureReason;
}
