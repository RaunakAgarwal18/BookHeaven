package com.bookheaven.email_service.dto.request;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String to;
    private String token;
}
