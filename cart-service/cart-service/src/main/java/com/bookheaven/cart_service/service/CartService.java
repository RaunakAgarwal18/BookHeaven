package com.bookheaven.cart_service.service;

import com.bookheaven.cart_service.dto.cartResponseDto.CartResponse;

import java.util.UUID;

public interface CartService {
    public CartResponse getCart(UUID userId);
    public CartResponse addToCart(UUID userId, Long listingId, int quantity);
    public CartResponse updateQuantity(UUID userId, Long listingId, int quantity);
    public CartResponse removeFromCart(UUID userId, Long listingId);
    public void clearCart(UUID userId);
    public CartResponse applyCoupon(UUID userId, String couponCode);
    public CartResponse removeCoupon(UUID userId);
}
