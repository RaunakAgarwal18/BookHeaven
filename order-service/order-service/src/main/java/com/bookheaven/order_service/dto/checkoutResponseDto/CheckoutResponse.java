package com.bookheaven.order_service.dto.checkoutResponseDto;

import com.bookheaven.order_service.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CheckoutResponse {
    private UUID orderId;
    private String orderReference;
    private String gatewayOrderId;
    private String keyId;
    private Double totalAmount;
    private String currency;
    private Order.OrderStatus status;
}