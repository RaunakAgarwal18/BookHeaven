package com.bookheaven.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerPayoutEvent {
    private UUID eventId;
    private UUID settlementId;
    private UUID sellerId;
    private Double netAmount;
    private String currency;
    private List<Long> ledgerIds;
    private LocalDate settlementDate;
}
