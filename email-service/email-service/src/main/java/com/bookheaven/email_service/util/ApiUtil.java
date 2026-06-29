package com.bookheaven.email_service.util;

import com.bookheaven.common.dto.response.EmailResponse;

import java.time.LocalDateTime;

public class ApiUtil {
    public static EmailResponse success(String message) {
        return EmailResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
