package com.bookheaven.order_service.service;

import com.bookheaven.order_service.dto.Event.*;
import com.bookheaven.order_service.entity.Order;
import com.bookheaven.order_service.entity.OrderItem;
import com.bookheaven.order_service.entity.ShippingAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderEventMapper {

    /**
     * Formats a ShippingAddress into a human-readable string.
     * Shared across confirmed, shipped, and delivered events.
     */
    public String formatAddress(ShippingAddress addr) {
        if (addr == null) return "N/A";
        return addr.getStreet() + ", " + addr.getCity() + ", "
                + addr.getState() + " " + addr.getZipCode() + ", " + addr.getCountry();
    }

    public OrderConfirmedEvent buildConfirmedEvent(Order order) {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setTo(order.getEmail());
        event.setUsername(order.getUsername());
        event.setOrderId(order.getOrderReference());
        event.setShippingAddress(formatAddress(order.getShippingAddress()));
        return event;
    }

    public OrderShippedEvent buildShippedEvent(Order order, boolean isFinalShipment, List<OrderItem> newlyShipped) {
        List<OrderShippedEvent.ShippedItem> shippedItems = order.getItems().stream()
                .map(item -> OrderShippedEvent.ShippedItem.builder()
                        .bookTitle(item.getTitle())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<OrderShippedEvent.ShippedItem> newlyShippedItems = newlyShipped.stream()
                .map(item -> OrderShippedEvent.ShippedItem.builder()
                        .bookTitle(item.getTitle())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderShippedEvent.builder()
                .to(order.getEmail())
                .username(order.getUsername())
                .orderId(order.getOrderReference())
                .shippingAddress(formatAddress(order.getShippingAddress()))
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod())
                .items(shippedItems)
                .newlyShippedItems(newlyShippedItems)
                .isFinalShipment(isFinalShipment)
                .build();
    }

    public OrderDeliveredEvent buildDeliveredEvent(Order order, boolean isFinalDelivery, List<OrderItem> newlyDelivered) {
        List<OrderDeliveredEvent.DeliveredItem> deliveredItems = order.getItems().stream()
                .map(item -> OrderDeliveredEvent.DeliveredItem.builder()
                        .bookTitle(item.getTitle())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        List<OrderDeliveredEvent.DeliveredItem> newlyDeliveredItems = newlyDelivered.stream()
                .map(item -> OrderDeliveredEvent.DeliveredItem.builder()
                        .bookTitle(item.getTitle())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderDeliveredEvent.builder()
                .to(order.getEmail())
                .username(order.getUsername())
                .orderId(order.getOrderReference())
                .shippingAddress(formatAddress(order.getShippingAddress()))
                .items(deliveredItems)
                .newlyDeliveredItems(newlyDeliveredItems)
                .isFinalDelivery(isFinalDelivery)
                .build();
    }

    public SellerOrderEvent buildSellerEvent(Order order, String sellerEmail, List<OrderItem> sellerItems) {
        List<SellerOrderEvent.OrderedBookItem> bookItems = sellerItems.stream()
                .map(item -> SellerOrderEvent.OrderedBookItem.builder()
                        .bookTitle(item.getTitle())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return SellerOrderEvent.builder()
                .to(sellerEmail)
                .sellerUsername(sellerItems.getFirst().getSellerUsername())
                .orderId(order.getId().toString())
                .buyerUsername(order.getUsername())
                .currency(sellerItems.getFirst().getCurrency())
                .shippingAddress(formatAddress(order.getShippingAddress()))
                .items(bookItems)
                .build();
    }

    public OrderLedgerEvent buildLedgerEvent(Order order) {
        List<OrderLedgerEvent.LedgerItemDto> ledgerItems = order.getItems().stream()
                .map(item -> OrderLedgerEvent.LedgerItemDto.builder()
                        .orderItemId(item.getId())
                        .sellerId(item.getSellerId())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .currency(item.getCurrency())
                        .build())
                .toList();

        return OrderLedgerEvent.builder()
                .orderId(order.getId())
                .discountAmount(order.getDiscountAmount())
                .discountSellerId(order.getDiscountSellerId())
                .items(ledgerItems)
                .build();
    }
}
