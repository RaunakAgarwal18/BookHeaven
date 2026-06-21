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
public class SellerOrderEvent {
    private String to;
    private String sellerUsername;
    private String orderId;
    private String buyerUsername;
    private String currency;
    private String shippingAddress;
    private List<OrderedBookItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderedBookItem {
        private String bookTitle;
        private Integer quantity;
        private Double price;
    }
}
