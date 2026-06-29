package com.bookheaven.payment_service.strategy;

import com.bookheaven.common.dto.request.InitiatePaymentRequest;
import com.bookheaven.payment_service.dto.requestDto.RefundRequest;
import com.bookheaven.payment_service.dto.strategy.GatewayOrderResponse;
import com.bookheaven.payment_service.dto.strategy.WebhookPayload;
import com.bookheaven.payment_service.entity.Payment;

public interface PaymentGatewayStrategy {
    GatewayOrderResponse createOrder(InitiatePaymentRequest request);
    boolean verifyWebhookSignature(String payload, String signature);
    WebhookPayload parseWebhook(String payload);
    String refund(Payment payment, RefundRequest request, String idempotencyKey);
    String getName();
}
