package com.bookheaven.order_service.dto.OrderResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private Long bookId;
    private String title;
    private String author;
    private Integer quantity;
    private Double price;
    private String currency;
    private String sellerId;
    private String sellerUsername;
}