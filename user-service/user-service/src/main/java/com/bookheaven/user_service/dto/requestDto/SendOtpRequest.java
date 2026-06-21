package com.bookheaven.user_service.dto.requestDto;

import lombok.Data;

@Data
public class SendOtpRequest {
    private String to;
    private String otp;
}