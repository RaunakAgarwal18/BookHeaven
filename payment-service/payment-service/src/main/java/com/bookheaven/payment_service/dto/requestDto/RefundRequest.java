package com.bookheaven.payment_service.dto.requestDto;

import lombok.Data;
import java.util.UUID;

@Data
public class RefundRequest {
    private UUID eventId;
    private UUID orderId;
    private Double amount;
    private String reason;
}
