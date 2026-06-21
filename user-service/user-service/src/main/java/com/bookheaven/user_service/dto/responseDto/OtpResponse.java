package com.bookheaven.user_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpResponse {
    private String email;
    private boolean otpSent;
    private int expiresIn;
}
