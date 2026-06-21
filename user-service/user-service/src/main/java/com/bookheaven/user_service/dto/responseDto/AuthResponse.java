package com.bookheaven.user_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse<T> {
    private int status;
    private String message;
    private T data;
}
