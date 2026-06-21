package com.bookheaven.cart_service.controller;

import com.bookheaven.cart_service.dto.cartResponseDto.CartResponse;
import com.bookheaven.cart_service.dto.requestDto.AddToCartRequest;
import com.bookheaven.cart_service.dto.requestDto.UpdateCartRequest;
import com.bookheaven.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(extractUserId(authentication)));
    }

    @PostMapping("/item")
    public ResponseEntity<CartResponse> addToCart(Authentication authentication, @Valid @RequestBody AddToCartRequest request) {
        UUID userId = extractUserId(authentication);
        CartResponse response = cartService.addToCart(userId, request.getListingId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{listingId}")
    public ResponseEntity<CartResponse> updateCartItem(Authentication authentication, @PathVariable Long listingId, @Valid @RequestBody UpdateCartRequest request) {
        UUID userId = extractUserId(authentication);
        CartResponse response = cartService.updateQuantity(userId, listingId, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{listingId}")
    public ResponseEntity<CartResponse> removeFromCart(Authentication authentication, @PathVariable Long listingId) {
        UUID userId = extractUserId(authentication);
        CartResponse response = cartService.removeFromCart(userId, listingId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internal/{userId}")
    public ResponseEntity<Void> clearCartInternal(@PathVariable UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/coupon/{code}")
    public ResponseEntity<CartResponse> applyCoupon(Authentication authentication, @PathVariable String code) {
        UUID userId = extractUserId(authentication);
        CartResponse response = cartService.applyCoupon(userId, code);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<CartResponse> removeCoupon(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        CartResponse response = cartService.removeCoupon(userId);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}