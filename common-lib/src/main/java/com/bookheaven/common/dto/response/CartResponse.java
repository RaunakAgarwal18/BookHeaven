package com.bookheaven.common.dto.response;

import lombok.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID userId;
    private List<CartItemResponse> items;
    private Double subtotal;
    private String couponCode;
    private Double discountAmount;
    private UUID discountSellerId;
    private Double taxAmount;
    private Double shippingAmount;
    private Double totalAmount;
    private String currency;
}
