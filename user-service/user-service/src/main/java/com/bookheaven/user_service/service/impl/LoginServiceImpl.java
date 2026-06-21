package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.dto.requestDto.LoginRequest;
import com.bookheaven.user_service.dto.responseDto.AuthResponse;
import com.bookheaven.user_service.dto.responseDto.AuthData;
import com.bookheaven.user_service.entity.User;
import com.bookheaven.user_service.exception.InvalidCredentialsException;
import com.bookheaven.user_service.service.LoginService;
import com.bookheaven.user_service.service.RedisService;
import com.bookheaven.user_service.service.UserService;
import com.bookheaven.user_service.constant.AppConstants;
import com.bookheaven.user_service.util.AuthResponseBuilder;
import com.bookheaven.user_service.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.bookheaven.user_service.exception.CacheServiceException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private RedisService redisService;

    public AuthResponse<AuthData> validateUser(LoginRequest request) {
        User savedUser = userService.getUserByEmail(request.getEmail());
        if (savedUser == null) {
            log.error("No user found for email {}", request.getEmail());
            throw new InvalidCredentialsException("Email or password incorrect");
        }
        if (savedUser.getAuthProvider() != User.AuthProvider.LOCAL) {
            log.error("OAuth user {} attempted password login", request.getEmail());
            throw new InvalidCredentialsException("Email or password incorrect");
        }
        if (!passwordEncoder.matches(request.getPassword(), savedUser.getPassword())) {
            log.error("Invalid password for email {}", request.getEmail());
            throw new InvalidCredentialsException("Email or password incorrect");
        }
        log.info("Generating access tokens for : {}", request.getEmail());
        String role = savedUser.getPrimaryRole();
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUsername(), role);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUsername(), role);
        try {
            redisService.saveKeyWithTimeout(AppConstants.REDIS_KEY_REFRESH + savedUser.getEmail(), refreshToken, AppConstants.REFRESH_TOKEN_EXPIRY_MINUTES);
        }catch (Exception ex){
            throw new CacheServiceException("Failed to save session token. Please try again.");
        }
        log.info("Tokens generated for : {} and saved in redis", request.getEmail());
        return AuthResponseBuilder.build(200,
                "Login Successfully!",
                savedUser.getId().toString(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getProfilePicture(),
                accessToken,
                refreshToken,
                role);
    }
}