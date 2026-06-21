package com.bookheaven.user_service.service;

import com.bookheaven.user_service.dto.requestDto.AddressRequest;
import com.bookheaven.user_service.entity.User;
import org.springframework.security.core.Authentication;

public interface AddressService {
    User addAddress(Authentication authentication, AddressRequest request);
    User updateAddress(Authentication authentication, Long addressId, AddressRequest request);
    User deleteAddress(Authentication authentication, Long addressId);
}
