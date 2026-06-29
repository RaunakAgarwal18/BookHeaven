package com.bookheaven.user_service.util;

import com.bookheaven.user_service.dto.responseDto.AuthResponse;
import com.bookheaven.user_service.dto.responseDto.AuthData;
import com.bookheaven.user_service.dto.responseDto.TokenDto;
import com.bookheaven.user_service.dto.responseDto.UserDto;

public class AuthResponseBuilder {

    public static AuthResponse<AuthData> build(
            int status,
            String message,
            String userId,
            String username,
            String email,
            String profilePicture,
            String accessToken,
            String refreshToken,
            String role,
            Boolean requiresPasswordSetup
    ) {
        UserDto user = UserDto.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .profilePicture(profilePicture)
                .role(role)
                .requiresPasswordSetup(requiresPasswordSetup)
                .build();

        TokenDto tokens = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        AuthData authData = AuthData.builder()
                .user(user)
                .tokens(tokens)
                .build();

        return AuthResponse.<AuthData>builder()
                .status(status)
                .message(message)
                .data(authData)
                .build();
    }
}
