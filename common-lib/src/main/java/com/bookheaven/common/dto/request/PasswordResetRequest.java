package com.bookheaven.common.dto.request;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String to;
    private String token;
}
