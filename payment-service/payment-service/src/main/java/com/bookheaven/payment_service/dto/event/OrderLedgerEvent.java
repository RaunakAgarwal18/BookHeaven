package com.bookheaven.payment_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLedgerEvent {
    private UUID orderId;
    private Double discountAmount;
    private UUID discountSellerId;
    private List<LedgerItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LedgerItemDto {
        private Long orderItemId;
        private UUID sellerId;
        private Double price;
        private Integer quantity;
        private String currency;
    }
}
