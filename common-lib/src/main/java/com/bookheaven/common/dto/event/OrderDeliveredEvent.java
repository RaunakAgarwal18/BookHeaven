package com.bookheaven.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveredEvent {
    private String to;
    private String username;
    private String orderId;
    private String shippingAddress;
    private List<DeliveredItem> items;
    private List<DeliveredItem> newlyDeliveredItems;
    private boolean isFinalDelivery;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveredItem {
        private String bookTitle;
        private Integer quantity;
    }
}
