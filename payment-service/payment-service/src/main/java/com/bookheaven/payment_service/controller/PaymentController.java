package com.bookheaven.payment_service.controller;

import com.bookheaven.payment_service.dto.requestDto.InitiatePaymentRequest;
import com.bookheaven.payment_service.dto.requestDto.RefundRequest;
import com.bookheaven.payment_service.dto.responseDto.InitiatePaymentResponse;
import com.bookheaven.payment_service.dto.responseDto.PaymentResponse;
import com.bookheaven.payment_service.service.PaymentService;
import com.bookheaven.payment_service.component.EndOfDaySettlementScheduler;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final EndOfDaySettlementScheduler settlementScheduler;

    @Value("${internal.service.secret}")
    private String internalServiceSecret;

    @Value("${stripe.publishable.key:}")
    private String stripePublishableKey;

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @GetMapping("/config")
    public ResponseEntity<java.util.Map<String, String>> getConfig() {
        return ResponseEntity.ok(java.util.Map.of(
                "stripePublishableKey", stripePublishableKey != null ? stripePublishableKey : "",
                "razorpayKeyId", razorpayKeyId != null ? razorpayKeyId : ""
        ));
    }

    // Called by order service
    @PostMapping("/initiate")
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(
            @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(request));
    }

    // Called by Razorpay
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook("razorpay", payload, signature);
        return ResponseEntity.ok().build();
    }

    // Called by generic provider webhook (e.g. Stripe, PayPal)
    @PostMapping("/webhook/{provider}")
    public ResponseEntity<Void> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySig) {
        String finalSig = signature != null ? signature : razorpaySig;
        paymentService.handleWebhook(provider, payload, finalSig);
        return ResponseEntity.ok().build();
    }

    // Called by order service on cancellation
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refund(@RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(request));
    }

    // Called by order service or frontend
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/admin/trigger-settlement")
    public ResponseEntity<String> triggerSettlement(
            @RequestHeader("X-Service-Secret") String serviceSecret) {
        if (!internalServiceSecret.equals(serviceSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid service secret");
        }
        settlementScheduler.executeSettlementAggregation();
        return ResponseEntity.ok("Consolidation triggered successfully!");
    }
}