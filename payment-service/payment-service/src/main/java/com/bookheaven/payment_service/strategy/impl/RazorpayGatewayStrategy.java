package com.bookheaven.payment_service.strategy.impl;

import com.bookheaven.payment_service.dto.requestDto.InitiatePaymentRequest;
import com.bookheaven.payment_service.dto.requestDto.RefundRequest;
import com.bookheaven.payment_service.dto.strategy.GatewayOrderResponse;
import com.bookheaven.payment_service.dto.strategy.WebhookPayload;
import com.bookheaven.payment_service.entity.Payment;
import com.bookheaven.payment_service.exception.PaymentInitiationException;
import com.bookheaven.payment_service.exception.RefundFailedException;
import com.bookheaven.payment_service.strategy.PaymentGatewayStrategy;
import com.bookheaven.payment_service.util.RazorpayUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RazorpayGatewayStrategy implements PaymentGatewayStrategy {

    private final RazorpayClient razorpayClient;
    private final RazorpayUtil razorpayUtil;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Override
    public GatewayOrderResponse createOrder(InitiatePaymentRequest request) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(request.getAmount() * 100)); // convert to paise
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", request.getOrderId().toString());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String gatewayOrderId = razorpayOrder.get("id");

            return GatewayOrderResponse.builder()
                    .gatewayOrderId(gatewayOrderId)
                    .publicKey(keyId)
                    .build();
        } catch (RazorpayException e) {
            throw new PaymentInitiationException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        return razorpayUtil.verifyWebhookSignature(payload, signature);
    }

    @Override
    public WebhookPayload parseWebhook(String payload) {
        JSONObject webhookBody = new JSONObject(payload);
        String event = webhookBody.getString("event");
        JSONObject paymentEntity = webhookBody
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String gatewayOrderId = paymentEntity.getString("order_id");
        String gatewayPaymentId = paymentEntity.getString("id");
        String paymentMethod = paymentEntity.getString("method");
        String failureReason = paymentEntity.has("error_description") && !paymentEntity.isNull("error_description")
                ? paymentEntity.getString("error_description")
                : null;

        return WebhookPayload.builder()
                .event(event)
                .gatewayOrderId(gatewayOrderId)
                .gatewayPaymentId(gatewayPaymentId)
                .paymentMethod(paymentMethod)
                .failureReason(failureReason)
                .build();
    }

    @Override
    public String refund(Payment payment, RefundRequest request) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (int)(request.getAmount() * 100)); // paise
            refundRequest.put("notes", new JSONObject().put("reason", request.getReason()));

            com.razorpay.Refund razorpayPayment = razorpayClient.payments
                    .refund(payment.getGatewayPaymentId(), refundRequest);

            return razorpayPayment.get("id");
        } catch (RazorpayException e) {
            throw new RefundFailedException("Razorpay refund failed: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "razorpay";
    }
}
