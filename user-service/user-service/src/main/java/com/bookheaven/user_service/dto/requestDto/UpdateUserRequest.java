package com.bookheaven.user_service.dto.requestDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String razorpayAccountId;
}
