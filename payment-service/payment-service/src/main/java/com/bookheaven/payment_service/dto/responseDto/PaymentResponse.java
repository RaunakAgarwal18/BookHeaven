package com.bookheaven.payment_service.dto.responseDto;

import com.bookheaven.payment_service.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private Double amount;
    private String currency;
    private Payment.PaymentStatus status;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String paymentMethod;
    private String failureReason;
    private LocalDateTime createdAt;
}