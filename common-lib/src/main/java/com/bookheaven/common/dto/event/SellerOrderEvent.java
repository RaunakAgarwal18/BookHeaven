package com.bookheaven.common.dto.event;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderedBookItem {
        private String bookTitle;
        private Integer quantity;
        private Double price;
    }
}


