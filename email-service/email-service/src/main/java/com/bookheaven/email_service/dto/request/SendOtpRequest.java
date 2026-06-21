package com.bookheaven.email_service.dto.request;

import lombok.Data;

@Data
public class SendOtpRequest {
    private String to;
    private String otp;
}
