package com.bookheaven.user_service.dto.requestDto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String userName;
    private String email;
    private String otp;
}