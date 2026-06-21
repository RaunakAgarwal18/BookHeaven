package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.dto.requestDto.AddressRequest;
import com.bookheaven.user_service.entity.Address;
import com.bookheaven.user_service.entity.User;
import com.bookheaven.user_service.exception.AddressNotFoundException;
import com.bookheaven.user_service.service.AddressService;
import com.bookheaven.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final UserService userService;

    @Override
    public User addAddress(Authentication authentication, AddressRequest request) {
        User user = userService.getSelfUser(authentication);
        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .build();
        user.getAddresses().add(address);
        return userService.saveUser(user);
    }

    @Override
    public User updateAddress(Authentication authentication, Long addressId, AddressRequest request) {
        User user = userService.getSelfUser(authentication);
        Address address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + addressId));

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());

        return userService.saveUser(user);
    }

    @Override
    public User deleteAddress(Authentication authentication, Long addressId) {
        User user = userService.getSelfUser(authentication);
        user.getAddresses().removeIf(a -> a.getId().equals(addressId));
        return userService.saveUser(user);
    }
}
