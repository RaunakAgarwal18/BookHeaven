package com.bookheaven.user_service.service;

import com.bookheaven.user_service.dto.requestDto.LoginRequest;
import com.bookheaven.user_service.dto.responseDto.AuthResponse;
import com.bookheaven.user_service.dto.responseDto.AuthData;

public interface LoginService {
    public AuthResponse<AuthData> validateUser(LoginRequest request);
}
