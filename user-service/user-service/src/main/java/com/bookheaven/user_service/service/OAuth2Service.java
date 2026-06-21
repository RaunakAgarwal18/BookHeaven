package com.bookheaven.user_service.service;

import com.bookheaven.user_service.dto.requestDto.OAuth2CallbackRequest;
import com.bookheaven.user_service.dto.responseDto.AuthData;
import com.bookheaven.user_service.dto.responseDto.AuthResponse;

public interface OAuth2Service {
    AuthResponse<AuthData> processOAuth2Callback(OAuth2CallbackRequest request);
}
