package com.bookheaven.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuth2UserInfo {
    private String providerId;   // unique ID from the provider
    private String email;
    private String name;
    private String picture;      // profile picture URL
}
