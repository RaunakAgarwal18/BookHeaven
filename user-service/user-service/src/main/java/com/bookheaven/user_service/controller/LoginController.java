package com.bookheaven.user_service.controller;

import com.bookheaven.user_service.dto.requestDto.LoginRequest;
import com.bookheaven.user_service.dto.responseDto.AuthResponse;
import com.bookheaven.user_service.dto.responseDto.AuthData;
import com.bookheaven.user_service.service.LoginService;
import com.bookheaven.user_service.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/user/login")
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    public ResponseEntity<AuthResponse<AuthData>> loginUser(@RequestBody LoginRequest request, HttpServletResponse servletResponse) {
        log.info("Login attempt for email: {}", request.getEmail());
        AuthResponse<AuthData> response = loginService.validateUser(request);
        
        if (response != null && response.getData() != null && response.getData().getTokens() != null) {
            String accessToken = response.getData().getTokens().getAccessToken();
            String refreshToken = response.getData().getTokens().getRefreshToken();
            
            // Set access token cookie (Path: /)
            CookieUtil.setAccessTokenCookie(servletResponse, accessToken, 86400); // 1 day
            // Set refresh token cookie (Path: /api/user/auth/refresh)
            CookieUtil.setRefreshTokenCookie(servletResponse, refreshToken, 604800); // 7 days
        }
        
        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}
