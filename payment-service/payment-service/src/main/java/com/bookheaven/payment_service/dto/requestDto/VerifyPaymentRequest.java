package com.bookheaven.payment_service.dto.requestDto;

import lombok.Data;

@Data
public class VerifyPaymentRequest {
    private String gatewayOrderId;   // razorpay_order_id
    private String gatewayPaymentId; // razorpay_payment_id
    private String gatewaySignature; // razorpay_signature
}