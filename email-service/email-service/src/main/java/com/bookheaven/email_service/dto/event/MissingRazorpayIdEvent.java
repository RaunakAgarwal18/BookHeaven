package com.bookheaven.email_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MissingRazorpayIdEvent {
    private String email;
    private String username;
    private Double amountPending;
    private String currency;
}
