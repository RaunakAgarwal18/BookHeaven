package com.bookheaven.order_service.dto.paymentRequestDto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class RefundPaymentRequest {
        private UUID orderId;
        private Double amount;
        private String reason;
}
