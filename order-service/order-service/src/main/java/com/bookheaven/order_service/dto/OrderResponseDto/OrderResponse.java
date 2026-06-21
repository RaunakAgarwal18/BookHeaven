package com.bookheaven.order_service.dto.OrderResponseDto;

import com.bookheaven.order_service.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderReference;
    private String username;
    private List<OrderItemResponse> items;
    private ShippingAddressResponse shippingAddress;
    private Order.OrderStatus status;
    private Double subtotal;
    private Double discountAmount;
    private String couponCode;
    private UUID discountSellerId;
    private Double taxAmount;
    private Double shippingAmount;
    private Double totalAmount;
    private String currency;
    private String paymentMethod;
    private String paymentId;
    private LocalDateTime createdAt;
}