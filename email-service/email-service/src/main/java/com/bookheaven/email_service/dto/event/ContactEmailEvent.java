package com.bookheaven.email_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactEmailEvent {
    private String email;
    private String subject;
    private String description;
    private String screenshotBase64;
    private String filename;
}
