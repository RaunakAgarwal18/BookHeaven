package com.bookheaven.order_service.dto.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShippedEvent {
    private String to;
    private String username;
    private String orderId;
    private String shippingAddress;
    private Double subtotal;
    private Double taxAmount;
    private Double shippingAmount;
    private Double discountAmount;
    private Double totalAmount;
    private String currency;
    private String paymentMethod;
    private List<ShippedItem> items;
    private List<ShippedItem> newlyShippedItems;
    private boolean isFinalShipment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippedItem {
        private String bookTitle;
        private Integer quantity;
        private Double price;
    }
}
