package com.bookheaven.order_service.util;

import com.bookheaven.order_service.dto.OrderResponseDto.OrderItemResponse;
import com.bookheaven.order_service.dto.OrderResponseDto.OrderResponse;
import com.bookheaven.order_service.dto.OrderResponseDto.ShippingAddressResponse;
import com.bookheaven.order_service.entity.Order;
import com.bookheaven.order_service.entity.OrderItem;
import com.bookheaven.order_service.entity.ShippingAddress;

public class AppUtil {
    public static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderReference(order.getOrderReference())
                .username(order.getUsername())
                .items(order.getItems().stream()
                        .map(AppUtil::toOrderItemResponse)
                        .toList())
                .shippingAddress(toShippingAddressResponse(order.getShippingAddress()))
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .couponCode(order.getCouponCode())
                .discountSellerId(order.getDiscountSellerId())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod())
                .paymentId(order.getPaymentId())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private  static OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .bookId(item.getBookId())
                .title(item.getTitle())
                .author(item.getAuthor())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .currency(item.getCurrency())
                .sellerId(item.getSellerId() != null ? item.getSellerId().toString() : null)
                .sellerUsername(item.getSellerUsername())
                .build();
    }

    private static ShippingAddressResponse toShippingAddressResponse(ShippingAddress address) {
        if (address == null) return null;
        return ShippingAddressResponse.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .phoneNumber(address.getPhoneNumber())
                .build();
    }
}
