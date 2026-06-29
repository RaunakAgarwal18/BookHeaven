package com.bookheaven.common.dto.request;

import lombok.Data;

@Data
public class SendOtpRequest {
    private String to;
    private String otp;
}
