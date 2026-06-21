package com.bookheaven.email_service.dto.event;

import lombok.Data;

import java.util.List;

@Data
public class SellerOrderEvent {
    private String to;
    private String sellerUsername;
    private String orderId;
    private String buyerUsername;
    private String currency;
    private String shippingAddress;
    private List<OrderedBookItem> items;

    @Data
    public static class OrderedBookItem {
        private String bookTitle;
        private Integer quantity;
        private Double price;
    }
}

