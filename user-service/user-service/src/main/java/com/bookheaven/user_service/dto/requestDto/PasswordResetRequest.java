package com.bookheaven.user_service.dto.requestDto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String to;
    private String token;
}
