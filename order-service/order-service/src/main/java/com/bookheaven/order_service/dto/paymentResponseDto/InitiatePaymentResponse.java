package com.bookheaven.order_service.dto.paymentResponseDto;

import lombok.Data;

import java.util.UUID;

@Data
public class InitiatePaymentResponse {
    private UUID paymentId;
    private String gatewayOrderId;
    private String keyId;
    private Double amount;
    private String currency;
}