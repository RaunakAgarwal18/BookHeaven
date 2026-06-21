package com.bookheaven.order_service.dto.paymentRequestDto;

import lombok.Data;

import java.util.UUID;

@Data
public class InitiatePaymentRequest {
    private UUID orderId;
    private UUID userId;
    private Double amount;
    private String currency;
    private String paymentMethod;
}