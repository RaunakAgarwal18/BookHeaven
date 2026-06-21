package com.bookheaven.user_service.dto.responseDto;

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
    private String profilePicture;
    private java.util.List<AddressDto> addresses;
    private String role;
    private String razorpayAccountId;
}
