package com.bookheaven.user_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String userId;
    private String username;
    private String email;
    private String profilePicture;
    private String role;
    private Boolean requiresPasswordSetup;
}
