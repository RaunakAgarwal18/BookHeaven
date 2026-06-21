package com.bookheaven.payment_service.dto.strategy;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayOrderResponse {
    private String gatewayOrderId;
    private String publicKey; // e.g. keyId for Razorpay, publishableKey for Stripe
}
