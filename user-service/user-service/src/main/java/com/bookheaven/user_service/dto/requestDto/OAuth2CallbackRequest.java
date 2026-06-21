package com.bookheaven.user_service.dto.requestDto;

import lombok.Data;

@Data
public class OAuth2CallbackRequest {
    private String provider;   // "google"
    private String code;       // The authorization code from the OAuth provider
}
