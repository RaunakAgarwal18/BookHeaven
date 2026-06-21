package com.bookheaven.user_service.dto.requestDto;

import lombok.Data;

@Data
public class LoginRequest {
    String email;
    String password;
}
