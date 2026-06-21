package com.bookheaven.user_service.dto.requestDto;

import lombok.Data;

@Data
public class ContactRequest {
    private String email;
    private String subject;
    private String description;
    private String screenshotBase64;
    private String filename;
}
