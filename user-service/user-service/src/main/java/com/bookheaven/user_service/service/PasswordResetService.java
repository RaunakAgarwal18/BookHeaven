package com.bookheaven.user_service.service;

import com.bookheaven.user_service.dto.requestDto.ForgotPasswordRequest;
import com.bookheaven.user_service.dto.requestDto.ResetPasswordRequest;

public interface PasswordResetService {
    public void forgotPassword(ForgotPasswordRequest request);
    public void resetPassword(String token , ResetPasswordRequest request);

}
