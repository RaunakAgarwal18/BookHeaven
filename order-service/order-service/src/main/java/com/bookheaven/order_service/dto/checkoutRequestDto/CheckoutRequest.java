package com.bookheaven.order_service.dto.checkoutRequestDto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String paymentMethod;
    private Long addressId;
}