package com.bookheaven.user_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookheaven.common.dto.event.WelcomeEmailEvent;
import com.bookheaven.user_service.dto.requestDto.SignupRequest;
import com.bookheaven.user_service.dto.responseDto.*;
import com.bookheaven.common.dto.response.UserResponse;
import com.bookheaven.user_service.entity.User;
import com.bookheaven.user_service.exception.InvalidTokenException;
import com.bookheaven.user_service.exception.SignupSessionExpiredException;
import com.bookheaven.user_service.exception.UserAlreadyExistException;
import com.bookheaven.user_service.service.*;
import com.bookheaven.user_service.constant.AppConstants;
import com.bookheaven.user_service.util.AuthResponseBuilder;
import com.bookheaven.user_service.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bookheaven.user_service.exception.SignupProcessingException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private OtpService otpService;
    private RedisService redisService;
    private JwtUtil jwtUtil;
    private WelcomeEmailProducer welcomeEmailProducer;

    @Override
    public AuthResponse<OtpResponse> initiateSignUp(SignupRequest request){
        if(userService.userExistsByEmail(request.getEmail()) || userService.userExistsByUsername(request.getUsername())){
            log.error("User with email {} and username {} already exists", request.getEmail(), request.getUsername());
            throw new UserAlreadyExistException("Email or Username already exist");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(request);
            redisService.saveKeyWithTimeout(AppConstants.REDIS_KEY_SIGNUP + request.getEmail(), json, AppConstants.SIGNUP_EXPIRY_MINUTES);
            otpService.sendOtp(request.getEmail());
        }catch (JsonProcessingException ex){
            log.error("Failed to process signup request for email -{}", request.getEmail(), ex);
            throw new SignupProcessingException("Failed to process signup request. Try again");
        }catch (MessagingException ex){
            log.error("Failed to send otp for email -{}", request.getEmail(), ex);
            throw new SignupProcessingException("Failed to send OTP email, please try again");
        }
        log.info("Otp sent for : {}", request.getEmail());
        return AuthResponse.<OtpResponse>builder()
                .data(OtpResponse.builder()
                        .email(request.getEmail())
                        .otpSent(true)
                        .expiresIn(300)
                        .build())
                .status(200)
                .message("Otp send successfully!!")
                .build();
    }

    public AuthResponse<AuthData> verifyOtpAndCreateUser(String userName, String email, String otp) {
        log.info("Otp validation starting for : {}", email);
        otpService.validateOtp(email, otp);
        log.info("Otp validation ended for : {}", email);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = (String) redisService.getKey(AppConstants.REDIS_KEY_SIGNUP + email);
        SignupRequest request;
        try {
            request = objectMapper.readValue(json, SignupRequest.class);
        }catch(JsonProcessingException ex){
            log.error("Error processing signup request for : {}", email);
            throw new SignupProcessingException("Something went wrong. Try again later");
        }
        if (request == null) {
            log.error("Timeout for signup request for : {}", email);
            throw new SignupSessionExpiredException("Signup session expired, please sign up again");
        }
        Set<User.Role> assignedRoles = new HashSet<>();
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("SELLER")) {
            assignedRoles.add(User.Role.SELLER);
        } else {
            assignedRoles.add(User.Role.USER);
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(assignedRoles)
                .build();
                
        if (request.getAddress() != null) {
            request.getAddress().setUser(user);
            user.setAddresses(new java.util.ArrayList<>(java.util.List.of(request.getAddress())));
        } else {
            user.setAddresses(new java.util.ArrayList<>());
        }
        User savedUser = userService.saveUser(user);
        redisService.deleteKey(AppConstants.REDIS_KEY_SIGNUP + email);
        log.info("Generating tokens for : {}", email);
        String userRole = savedUser.getPrimaryRole();
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), email, userName, userRole);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId(), email, userName, userRole);
        redisService.saveKeyWithTimeout(AppConstants.REDIS_KEY_REFRESH + savedUser.getEmail(), refreshToken, AppConstants.REFRESH_TOKEN_EXPIRY_DAYS_SHORT);
        log.info("Tokens generated for {} and saved in redis", email);
        WelcomeEmailEvent event = new WelcomeEmailEvent();
        event.setTo(savedUser.getEmail());
        event.setUsername(savedUser.getUsername());
        event.setRole(userRole);
        welcomeEmailProducer.publishWelcomeEmail(event);
        return AuthResponseBuilder.build(201,
                                        "User created Successfully!",
                                                savedUser.getId().toString(),
                                                savedUser.getUsername(),
                                                savedUser.getEmail(),
                                                savedUser.getProfilePicture(),
                                                accessToken,
                                                refreshToken,
                                                userRole);
    }

    @Override
    public AuthResponse<TokenDto> refreshAccessToken(String refreshToken) {
        try {
            UUID id = jwtUtil.extractId(refreshToken);
            String email = jwtUtil.extractEmail(refreshToken);
            String username = jwtUtil.extractUsername(refreshToken);
            String storedToken = redisService.getKey(AppConstants.REDIS_KEY_REFRESH + email);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                throw new InvalidTokenException("Invalid refresh token, please log in again");
            }
            log.info("Generating tokens for : {}", email);
            User user = userService.getUserByEmail(email);
            String role = (user != null) ? user.getPrimaryRole() : "USER";
            String newAccessToken = jwtUtil.generateAccessToken(id, email, username, role);
            String newRefreshToken = jwtUtil.generateRefreshToken(id, email, username, role);
            redisService.saveKeyWithTimeout(AppConstants.REDIS_KEY_REFRESH + email, newRefreshToken, AppConstants.REFRESH_TOKEN_EXPIRY_DAYS_SHORT);
            log.info("Tokens generated for {} and saved in redis", email);
            return AuthResponse.<TokenDto>builder()
                    .status(200)
                    .message("Refresh token generated successfully")
                    .data(TokenDto.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(newRefreshToken)
                            .build())
                    .build();
        } catch (ExpiredJwtException ex) {
            log.error("Refresh token expired for : {}",jwtUtil.extractEmail(refreshToken));
            String email = ex.getClaims().getSubject();
            redisService.deleteKey(AppConstants.REDIS_KEY_REFRESH + email);
            throw new InvalidTokenException("Refresh token expired, please log in again");
        }
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                String email = jwtUtil.extractEmail(refreshToken);
                redisService.deleteKey(AppConstants.REDIS_KEY_REFRESH + email);
                log.info("Deleted refresh token from Redis for user: {}", email);
            } catch (Exception ex) {
                log.error("Failed to delete refresh token from Redis during logout: {}", ex.getMessage());
            }
        }
    }
}
