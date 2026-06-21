package com.bookheaven.user_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthData {
    private UserDto user;
    private TokenDto tokens;
}
