package com.bookheaven.payment_service.strategy.impl;

import com.bookheaven.common.dto.request.InitiatePaymentRequest;
import com.bookheaven.payment_service.dto.requestDto.RefundRequest;
import com.bookheaven.payment_service.dto.strategy.GatewayOrderResponse;
import com.bookheaven.payment_service.dto.strategy.WebhookPayload;
import com.bookheaven.payment_service.entity.Payment;
import com.bookheaven.payment_service.exception.PaymentInitiationException;
import com.bookheaven.payment_service.exception.RefundFailedException;
import com.bookheaven.payment_service.strategy.PaymentGatewayStrategy;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StripeGatewayStrategy implements PaymentGatewayStrategy {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public String getName() {
        return "stripe";
    }

    @Override
    public GatewayOrderResponse createOrder(InitiatePaymentRequest request) {
        try {
            // Stripe expects amount in smallest currency unit (e.g., cents for USD)
            long amountInCents = Math.round(request.getAmount() * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .putMetadata("orderId", request.getOrderId().toString())
                    .putMetadata("userId", request.getUserId().toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // For Stripe, the 'gatewayOrderId' will be the PaymentIntent ID,
            // and the 'publicKey' passed to frontend will be the Client Secret.
            return GatewayOrderResponse.builder()
                    .gatewayOrderId(paymentIntent.getId())
                    .publicKey(paymentIntent.getClientSecret())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while creating PaymentIntent", e);
            throw new PaymentInitiationException("Stripe error: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Webhook.Signature.verifyHeader(payload, signature, webhookSecret, 300);
            return true;
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed", e);
            return false;
        }
    }

    @Override
    public WebhookPayload parseWebhook(String payload) {
        // Since verifyWebhookSignature is called before this, we can assume payload is valid.
        // We will just parse the Event object.
        Event event = Event.GSON.fromJson(payload, Event.class);

        String internalEventName;
        String gatewayOrderId = null;
        String gatewayPaymentId = null;
        String paymentMethod = null;
        String failureReason = null;

        if ("payment_intent.succeeded".equals(event.getType())) {
            internalEventName = "payment.captured";
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                PaymentIntent paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
                gatewayOrderId = paymentIntent.getId();
                gatewayPaymentId = paymentIntent.getId(); // For Stripe PaymentIntent, order ID and payment ID are usually the same object
                paymentMethod = "stripe";
            }
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            internalEventName = "payment.failed";
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                PaymentIntent paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
                gatewayOrderId = paymentIntent.getId();
                gatewayPaymentId = paymentIntent.getId();
                failureReason = paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : "Payment failed";
            }
        } else {
            // Ignore other events
            internalEventName = "ignored";
        }

        return WebhookPayload.builder()
                .event(internalEventName)
                .gatewayOrderId(gatewayOrderId)
                .gatewayPaymentId(gatewayPaymentId)
                .paymentMethod(paymentMethod)
                .failureReason(failureReason)
                .build();
    }

    @Override
    public String refund(Payment payment, RefundRequest request, String idempotencyKey) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getGatewayPaymentId())
                    .build();

            com.stripe.net.RequestOptions requestOptions = com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(idempotencyKey)
                    .build();

            Refund refund = Refund.create(params, requestOptions);
            return refund.getId();
        } catch (StripeException e) {
            log.error("Stripe error while processing refund", e);
            throw new RefundFailedException("Stripe refund error: " + e.getMessage());
        }
    }
}
