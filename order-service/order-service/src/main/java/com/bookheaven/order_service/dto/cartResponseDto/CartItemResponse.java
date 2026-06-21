package com.bookheaven.order_service.dto.cartResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long listingId;
    private Long bookId;
    private String title;
    private String author;
    private String imageUrl;
    private Double price;
    private String currency;
    private Integer quantity;
    private Integer available;
    private String sellerUsername;
}