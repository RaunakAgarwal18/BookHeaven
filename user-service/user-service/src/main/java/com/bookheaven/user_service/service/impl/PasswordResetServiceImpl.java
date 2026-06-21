package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.dto.requestDto.ForgotPasswordRequest;
import com.bookheaven.user_service.dto.requestDto.PasswordResetRequest;
import com.bookheaven.user_service.dto.requestDto.ResetPasswordRequest;
import com.bookheaven.user_service.entity.User;
import com.bookheaven.user_service.exception.UserNotFoundException;
import com.bookheaven.user_service.exception.PasswordMismatchException;
import com.bookheaven.user_service.exception.ExpiredResetTokenException;
import com.bookheaven.user_service.service.PasswordResetService;
import com.bookheaven.user_service.service.RedisService;
import com.bookheaven.user_service.service.UserService;
import com.bookheaven.user_service.service.clientService.EmailClient;
import com.bookheaven.user_service.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final EmailClient emailClient;


    @Value("${password.reset.token.expiry.minutes:30}")
    private int tokenExpiry;

    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        try {
            User userOpt = userService.getUserByEmail(email);
        }catch (UserNotFoundException ex){
            log.info("Forgot password requested for non-existent email: {}", email);
            return;
        }

        String existingToken = redisService.getKey(AppConstants.REDIS_KEY_EMAIL_TOKEN + email);
        if (existingToken != null) {
            redisService.deleteKey(AppConstants.RESET_TOKEN_PREFIX + existingToken);
            redisService.deleteKey(AppConstants.REDIS_KEY_EMAIL_TOKEN + email);
        }

        // Generate new token
        String token = UUID.randomUUID().toString();
        redisService.saveKeyWithTimeout(AppConstants.RESET_TOKEN_PREFIX + token, email, tokenExpiry);
        redisService.saveKeyWithTimeout(AppConstants.REDIS_KEY_EMAIL_TOKEN + email, token, tokenExpiry);
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setTo(email);
        resetRequest.setToken(token);
        emailClient.sendPasswordResetMail(resetRequest);
        log.info("Password reset email sent to: {}", email);
    }

    public void resetPassword(String token, ResetPasswordRequest request) {
        // Check passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }
        String redisKey = AppConstants.RESET_TOKEN_PREFIX + token;

        // Get email from Redis using token
        String email = redisService.getKey(redisKey);
        if (email == null) {
            throw new ExpiredResetTokenException("Invalid or expired password reset token");
        }

        // Find user and update password
        User user = userService.getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.saveUser(user);

        // Delete both keys from Redis so token can't be reused
        redisService.deleteKey(redisKey);
        redisService.deleteKey(AppConstants.REDIS_KEY_EMAIL_TOKEN + email);
        log.info("Password reset successfully for: {}", email);
    }
}