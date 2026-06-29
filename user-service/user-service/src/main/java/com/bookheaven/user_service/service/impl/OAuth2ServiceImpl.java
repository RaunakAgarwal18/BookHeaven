package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.dto.OAuth2UserInfo;
import com.bookheaven.user_service.dto.requestDto.OAuth2CallbackRequest;
import com.bookheaven.user_service.dto.responseDto.AuthData;
import com.bookheaven.user_service.dto.responseDto.AuthResponse;
import com.bookheaven.user_service.entity.User;
import com.bookheaven.user_service.exception.UnsupportedProviderException;
import com.bookheaven.user_service.exception.UserAlreadyExistException;
import com.bookheaven.user_service.service.OAuth2Service;
import com.bookheaven.user_service.service.RedisService;
import com.bookheaven.user_service.service.UserService;
import com.bookheaven.user_service.service.oauth2.OAuth2ProviderStrategy;
import com.bookheaven.user_service.constant.AppConstants;
import com.bookheaven.user_service.util.AuthResponseBuilder;
import com.bookheaven.user_service.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, OAuth2ProviderStrategy> strategyMap;

    public OAuth2ServiceImpl(UserService userService, JwtUtil jwtUtil,
                             RedisService redisService, PasswordEncoder passwordEncoder,
                             List<OAuth2ProviderStrategy> strategies) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
        this.passwordEncoder = passwordEncoder;

        // Build lookup map from injected strategies
        this.strategyMap = new HashMap<>();
        for (OAuth2ProviderStrategy strategy : strategies) {
            this.strategyMap.put(strategy.getProviderName().toLowerCase(), strategy);
        }
    }

    @Override
    public AuthResponse<AuthData> processOAuth2Callback(OAuth2CallbackRequest request) {
        String provider = request.getProvider().toLowerCase();

        OAuth2ProviderStrategy strategy = strategyMap.get(provider);
        if (strategy == null) {
            throw new UnsupportedProviderException("Unsupported OAuth2 provider: " + provider);
        }

        OAuth2UserInfo userInfo = strategy.exchangeCodeForUserInfo(request.getCode());
        return findOrCreateUserAndGenerateTokens(userInfo, strategy.getAuthProvider());
    }

    private AuthResponse<AuthData> findOrCreateUserAndGenerateTokens(
            OAuth2UserInfo userInfo, User.AuthProvider authProvider) {

        // First try to find by email
        User existingUser = null;
        if (userService.userExistsByEmail(userInfo.getEmail())) {
            existingUser = userService.getUserByEmail(userInfo.getEmail());
        }

        User user;
        if (existingUser != null) {
            // User exists. Link account if necessary
            user = existingUser;
            // Update providerId if it was missing or changed
            if (user.getProviderId() == null || !user.getProviderId().equals(userInfo.getProviderId())) {
                user.setProviderId(userInfo.getProviderId());
                userService.saveUser(user);
            }
            log.info("Existing user found by email: {} - linking OAuth provider", user.getEmail());
        } else {
            // Create a brand new user

            // Create a brand new user
            String[] nameParts = userInfo.getName() != null
                    ? userInfo.getName().split(" ", 2) : new String[]{"", ""};
            String username = userInfo.getName() != null && !userInfo.getName().isBlank()
                    ? userInfo.getName()
                    : userInfo.getEmail().split("@")[0];

            user = User.builder()
                    .email(userInfo.getEmail())
                    .username(username)
                    .firstName(nameParts[0])
                    .lastName(nameParts.length > 1 ? nameParts[1] : "")
                    .password("NOT_SET")
                    .profilePicture(userInfo.getPicture())
                    .authProvider(authProvider)
                    .providerId(userInfo.getProviderId())
                    .roles(new HashSet<>(Set.of(User.Role.USER)))
                    .build();
            user = userService.saveUser(user);
            log.info("Created new OAuth user: {} via {}", user.getEmail(), authProvider);
        }

        // Generate tokens
        String role = user.getPrimaryRole();
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getUsername(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), user.getUsername(), role);
        redisService.saveKeyWithTimeout(AppConstants.REDIS_KEY_REFRESH + user.getEmail(), refreshToken, AppConstants.REFRESH_TOKEN_EXPIRY_MINUTES);

        return AuthResponseBuilder.build(
                200,
                "OAuth2 login successful!",
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePicture(),
                accessToken,
                refreshToken,
                role,
                "NOT_SET".equals(user.getPassword()) || user.getPassword() == null
        );
    }
}
