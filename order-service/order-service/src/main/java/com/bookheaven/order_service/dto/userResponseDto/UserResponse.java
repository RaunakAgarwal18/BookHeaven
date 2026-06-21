package com.bookheaven.order_service.dto.userResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private java.util.List<AddressResponse> addresses;
}