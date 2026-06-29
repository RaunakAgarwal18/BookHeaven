package com.bookheaven.common.dto.event;

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

    @Builder.Default
    private boolean reversal = false;   // true = void existing entries, not create new ones

    private List<Long> orderItemIds;    // non-null on partial voids (seller cancels specific items)

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
