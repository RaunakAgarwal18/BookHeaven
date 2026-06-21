package com.bookheaven.user_service.service;


import com.bookheaven.user_service.dto.requestDto.SignupRequest;
import com.bookheaven.user_service.dto.responseDto.AuthResponse;
import com.bookheaven.user_service.dto.responseDto.AuthData;
import com.bookheaven.user_service.dto.responseDto.OtpResponse;
import com.bookheaven.user_service.dto.responseDto.TokenDto;

public interface AuthService {
    public AuthResponse<OtpResponse> initiateSignUp(SignupRequest request);
    public AuthResponse<AuthData> verifyOtpAndCreateUser(String userName, String email, String otp);
    public AuthResponse<TokenDto> refreshAccessToken(String refreshToken);
    public void logout(String refreshToken);
}
