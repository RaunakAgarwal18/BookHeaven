package com.bookheaven.order_service.service;

import com.bookheaven.order_service.dto.OrderResponseDto.OrderResponse;
import com.bookheaven.order_service.dto.checkoutRequestDto.CheckoutRequest;
import com.bookheaven.order_service.dto.checkoutResponseDto.CheckoutResponse;
import com.bookheaven.order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface OrderService {
    public CheckoutResponse checkout(Authentication authentication, CheckoutRequest checkoutRequest);
    public void confirmOrder(UUID orderId, String gatewayPaymentId);
    public void failOrder(UUID orderId, String reason);
    public OrderResponse getOrder(Authentication authentication, UUID orderId);
    public Page<OrderResponse> getMyOrders(Authentication authentication, int pageNumber, int pageSize, String status);
    public OrderResponse cancelOrder(Authentication authentication, UUID orderId);
    public Page<OrderResponse> getAllOrders(int pageNumber, int pageSize, String status, UUID userId);
    public OrderResponse updateOrderStatus(UUID orderId, String status);
    public Page<OrderResponse> getSellerOrders(Authentication authentication, int pageNumber, int pageSize, String status);
    public OrderResponse updateSellerOrderStatus(Authentication authentication, UUID orderId, String status);
    public Order getOrderById(UUID orderId);
    public Order saveOrder(Order order);
}
