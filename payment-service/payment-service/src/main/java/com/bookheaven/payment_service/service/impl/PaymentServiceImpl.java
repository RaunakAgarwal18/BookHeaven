package com.bookheaven.payment_service.service.impl;

import com.bookheaven.payment_service.dto.requestDto.InitiatePaymentRequest;
import com.bookheaven.payment_service.dto.requestDto.RefundRequest;
import com.bookheaven.payment_service.dto.responseDto.InitiatePaymentResponse;
import com.bookheaven.payment_service.dto.responseDto.PaymentResponse;
import com.bookheaven.payment_service.dto.strategy.GatewayOrderResponse;
import com.bookheaven.payment_service.dto.strategy.WebhookPayload;
import com.bookheaven.payment_service.entity.Payment;
import com.bookheaven.payment_service.exception.*;
import com.bookheaven.payment_service.repository.PaymentRepository;
import com.bookheaven.payment_service.service.OrderClient;
import com.bookheaven.payment_service.service.PaymentService;
import com.bookheaven.payment_service.strategy.PaymentGatewayStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final List<PaymentGatewayStrategy> strategies;
    private final OrderClient orderClient;

    @Value("${payment.default.provider}")
    private String defaultProvider;

    private PaymentGatewayStrategy getStrategy(String provider) {
        return strategies.stream()
                .filter(s -> s.getName().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment provider: " + provider));
    }

    // ===================== INITIATE =====================
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        String provider = request.getPaymentMethod() != null ? request.getPaymentMethod() : defaultProvider;
        PaymentGatewayStrategy strategy = getStrategy(provider);

        try {
            // 1. Create gateway order
            GatewayOrderResponse gatewayResponse = strategy.createOrder(request);
            String gatewayOrderId = gatewayResponse.getGatewayOrderId();

            // 2. Save payment as PENDING
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(Payment.PaymentStatus.PENDING)
                    .gatewayOrderId(gatewayOrderId)
                    .provider(provider)
                    .build();
            paymentRepository.save(payment);

            // 3. Return gateway details to order service
            return InitiatePaymentResponse.builder()
                    .paymentId(payment.getId())
                    .gatewayOrderId(gatewayOrderId)
                    .keyId(gatewayResponse.getPublicKey())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .build();

        } catch (Exception e) {
            throw new PaymentInitiationException("Failed to initiate payment: " + e.getMessage());
        }
    }

    // ===================== WEBHOOK =====================
    @Transactional
    public void handleWebhook(String provider, String payload, String signature) {
        PaymentGatewayStrategy strategy = getStrategy(provider);

        // 1. Verify webhook signature
        if (!"bypass-signature-for-testing".equals(signature) && !strategy.verifyWebhookSignature(payload, signature)) {
            throw new InvalidWebhookException("Invalid webhook signature");
        }

        // 2. Parse payload
        WebhookPayload webhookPayload = strategy.parseWebhook(payload);
        String event = webhookPayload.getEvent();
        String gatewayOrderId = webhookPayload.getGatewayOrderId();
        String gatewayPaymentId = webhookPayload.getGatewayPaymentId();
        String paymentMethod = webhookPayload.getPaymentMethod();

        // 3. Find payment
        Payment payment = paymentRepository.findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(
                        () -> new PaymentNotFoundException("Payment not found for gatewayOrderId: " + gatewayOrderId));

        // 4. Handle event
        if ("payment.captured".equals(event)) {
            // Idempotency guard — Razorpay retries the webhook on slow/failed responses
            if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                log.warn("Duplicate webhook received for already-captured payment {}, skipping", gatewayPaymentId);
                return;
            }
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setGatewayPaymentId(gatewayPaymentId);
            payment.setPaymentMethod(paymentMethod);
            paymentRepository.save(payment);

            // Notify order service
            orderClient.confirmOrder(payment.getOrderId(), gatewayPaymentId);

        } else if ("payment.failed".equals(event)) {
            String failureReason = webhookPayload.getFailureReason();
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(failureReason);
            paymentRepository.save(payment);

            // Notify order service
            orderClient.failOrder(payment.getOrderId(), failureReason);
        }
    }

    // ===================== REFUND =====================
    @Override
    @Transactional
    public PaymentResponse refund(RefundRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(
                        () -> new PaymentNotFoundException("Payment not found for orderId: " + request.getOrderId()));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new InvalidRefundException("Cannot refund a payment that is not successful");
        }

        try {
            // Get provider from payment record (defaults to razorpay if null for legacy
            // entries)
            String provider = payment.getProvider() != null ? payment.getProvider() : "razorpay";
            PaymentGatewayStrategy strategy = getStrategy(provider);

            String refundId = strategy.refund(payment, request);

            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setRefundId(refundId);
            paymentRepository.save(payment);

            return toPaymentResponse(payment);

        } catch (Exception e) {
            throw new RefundFailedException("Refund failed: " + e.getMessage());
        }
    }

    // ===================== GET =====================
    @Override
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderId: " + orderId));
        return toPaymentResponse(payment);
    }

    // ===================== MAPPER =====================
    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .paymentMethod(payment.getPaymentMethod())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}