package com.bookheaven.cart_service.dto.cartResponseDto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

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
    private UUID sellerId;
    private String sellerUsername;
}
