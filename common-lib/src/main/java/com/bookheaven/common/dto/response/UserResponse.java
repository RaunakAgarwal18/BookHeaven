package com.bookheaven.common.dto.response;

import lombok.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String razorpayAccountId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private java.util.List<AddressResponse> addresses;
    private String profilePicture;
    private String role;
}
