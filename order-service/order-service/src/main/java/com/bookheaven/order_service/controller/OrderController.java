package com.bookheaven.order_service.controller;

import com.bookheaven.order_service.dto.OrderResponseDto.OrderResponse;
import com.bookheaven.order_service.dto.checkoutRequestDto.CheckoutRequest;
import com.bookheaven.order_service.dto.checkoutResponseDto.CheckoutResponse;
import com.bookheaven.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    // ===================== USER ENDPOINTS =====================

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            Authentication authentication,
            @RequestBody CheckoutRequest checkoutRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(authentication, checkoutRequest));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            Authentication authentication,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(authentication, orderId));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getMyOrders(authentication, pageNumber, pageSize, status));
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(authentication, orderId));
    }

    // ===================== INTERNAL ENDPOINTS =====================

    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(
            @PathVariable UUID orderId,
            @RequestParam String paymentId) {
        orderService.confirmOrder(orderId, paymentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/fail")
    public ResponseEntity<Void> failOrder(
            @PathVariable UUID orderId,
            @RequestParam String reason) {
        orderService.failOrder(orderId, reason);
        return ResponseEntity.ok().build();
    }

    // ===================== SELLER ENDPOINTS =====================

    @GetMapping("/seller")
    public ResponseEntity<Page<OrderResponse>> getSellerOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getSellerOrders(authentication, pageNumber, pageSize, status));
    }

    @PutMapping("/seller/{orderId}/status")
    public ResponseEntity<OrderResponse> updateSellerOrderStatus(
            Authentication authentication,
            @PathVariable UUID orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateSellerOrderStatus(authentication, orderId, status));
    }

    // ===================== ADMIN ENDPOINTS =====================

    @GetMapping("/admin/all")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(orderService.getAllOrders(pageNumber, pageSize, status, userId));
    }

    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    // ===================== PRIVATE HELPERS =====================


}