package com.bookheaven.user_service.util;

import com.bookheaven.common.dto.response.AddressResponse;
import com.bookheaven.common.dto.response.UserResponse;
import com.bookheaven.user_service.entity.User;

import java.util.stream.Collectors;

public class UserResponseBuilder {

    public static UserResponse mapUserDto(User user){
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .addresses(user.getAddresses() != null ? user.getAddresses().stream()
                        .map(a -> AddressResponse.builder()
                                .id(a.getId())
                                .street(a.getStreet())
                                .city(a.getCity())
                                .state(a.getState())
                                .zipCode(a.getZipCode())
                                .country(a.getCountry())
                                .build())
                        .collect(Collectors.toList()) : new java.util.ArrayList<>())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .role(user.getPrimaryRole())
                .razorpayAccountId(user.getRazorpayAccountId())
                .requiresPasswordSetup(user.getPassword() == null)
                .build();
    }
}

