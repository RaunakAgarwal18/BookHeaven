package com.bookheaven.payment_service.service;

import com.bookheaven.common.dto.request.InitiatePaymentRequest;
import com.bookheaven.payment_service.dto.requestDto.RefundRequest;
import com.bookheaven.common.dto.response.InitiatePaymentResponse;
import com.bookheaven.payment_service.dto.responseDto.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request);
    public void handleWebhook(String provider, String payload, String signature);
    public PaymentResponse refund(RefundRequest request);
    public PaymentResponse getPaymentByOrderId(UUID orderId);
}
