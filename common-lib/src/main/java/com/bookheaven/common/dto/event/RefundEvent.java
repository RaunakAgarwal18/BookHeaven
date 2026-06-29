package com.bookheaven.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundEvent {
    private UUID eventId;
    private UUID orderId;
    private Double amount;
    private String reason;
}
