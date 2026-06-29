package com.bookheaven.common.dto.response;

import lombok.Data;
import lombok.*;
import lombok.Builder;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentResponse {
    private UUID paymentId;
    private String gatewayOrderId;
    private String keyId;
    private Double amount;
    private String currency;
}
