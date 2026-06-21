package com.bookheaven.user_service.controller;

import com.bookheaven.user_service.dto.requestDto.ForgotPasswordRequest;
import com.bookheaven.user_service.dto.requestDto.OAuth2CallbackRequest;
import com.bookheaven.user_service.dto.requestDto.RefreshRequest;
import com.bookheaven.user_service.dto.requestDto.ResetPasswordRequest;
import com.bookheaven.user_service.dto.requestDto.SignupRequest;
import com.bookheaven.user_service.dto.requestDto.VerifyOtpRequest;
import com.bookheaven.user_service.dto.responseDto.AuthResponse;
import com.bookheaven.user_service.dto.responseDto.AuthData;
import com.bookheaven.user_service.dto.responseDto.OtpResponse;
import com.bookheaven.user_service.dto.responseDto.TokenDto;
import com.bookheaven.user_service.exception.InvalidTokenException;
import com.bookheaven.user_service.service.AuthService;
import com.bookheaven.user_service.service.OAuth2Service;
import com.bookheaven.user_service.service.PasswordResetService;
import com.bookheaven.user_service.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/user/auth")
public class AuthController {

    private AuthService authService;
    private final PasswordResetService passwordResetService;
    private final OAuth2Service oAuth2Service;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok("A reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,  @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(token, request);
        return ResponseEntity.ok("Password reset successfully. You can now login.");
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse<OtpResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Initiating Sign up for : {}", signupRequest.getEmail());
        AuthResponse<OtpResponse> response = authService.initiateSignUp(signupRequest);
        log.info("Otp sent successfully for : {}", signupRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse<AuthData>> verifyOtp(@RequestBody VerifyOtpRequest request, HttpServletResponse servletResponse){
        log.info("Initiated otp verification for : {}", request.getEmail());
        AuthResponse<AuthData> response = authService.verifyOtpAndCreateUser(request.getUserName(),request.getEmail(), request.getOtp());
        
        if (response != null && response.getData() != null && response.getData().getTokens() != null) {
            String accessToken = response.getData().getTokens().getAccessToken();
            String refreshToken = response.getData().getTokens().getRefreshToken();
            
            CookieUtil.setAccessTokenCookie(servletResponse, accessToken, 86400); // 1 day
            CookieUtil.setRefreshTokenCookie(servletResponse, refreshToken, 604800); // 7 days
        }
        
        log.info("Otp verified for : {}", request.getEmail());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse<TokenDto>> refresh(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshRequest request,
            HttpServletResponse servletResponse) {
        
        String refreshToken = cookieRefreshToken;
        if (refreshToken == null || refreshToken.isEmpty()) {
            if (request != null) {
                refreshToken = request.getRefreshToken();
            }
        }
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException("Refresh token is missing");
        }
        
        log.info("Refreshing access token");
        AuthResponse<TokenDto> response = authService.refreshAccessToken(refreshToken);
        
        if (response != null && response.getData() != null) {
            String newAccessToken = response.getData().getAccessToken();
            String newRefreshToken = response.getData().getRefreshToken();
            
            CookieUtil.setAccessTokenCookie(servletResponse, newAccessToken, 86400); // 1 day
            CookieUtil.setRefreshTokenCookie(servletResponse, newRefreshToken, 604800); // 7 days
        }
        
        log.info("New access token generated");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse servletResponse) {
        
        log.info("Logging out user");
        if (refreshToken != null && !refreshToken.isEmpty()) {
            authService.logout(refreshToken);
        }
        CookieUtil.clearCookies(servletResponse);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/oauth2/callback")
    public ResponseEntity<AuthResponse<AuthData>> oauth2Callback(
            @RequestBody OAuth2CallbackRequest request,
            HttpServletResponse servletResponse) {
        log.info("OAuth2 callback for provider: {}", request.getProvider());
        AuthResponse<AuthData> response = oAuth2Service.processOAuth2Callback(request);

        if(response != null && response.getData() != null && response.getData().getTokens() != null) {
            String accessToken = response.getData().getTokens().getAccessToken();
            String refreshToken = response.getData().getTokens().getRefreshToken();
            CookieUtil.setAccessTokenCookie(servletResponse, accessToken, 86400); // 1 day
            CookieUtil.setRefreshTokenCookie(servletResponse, refreshToken, 604800); // 7 days
        }
        return ResponseEntity.ok(response);
    }
}
