package com.bookheaven.payment_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InitiatePaymentResponse {
    private UUID paymentId;        // your internal payment id
    private String gatewayOrderId; // razorpay order_id
    private String keyId;          // razorpay key_id for frontend
    private Double amount;
    private String currency;
}