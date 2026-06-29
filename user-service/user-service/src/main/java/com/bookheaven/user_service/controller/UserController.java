package com.bookheaven.user_service.controller;

import com.bookheaven.user_service.dto.requestDto.AddressRequest;
import com.bookheaven.user_service.dto.requestDto.ChangePasswordRequest;
import com.bookheaven.user_service.dto.requestDto.ContactRequest;
import com.bookheaven.user_service.dto.requestDto.UpdateUserRequest;
import com.bookheaven.common.dto.response.UserResponse;
import com.bookheaven.user_service.service.AddressService;
import com.bookheaven.user_service.service.ContactFormService;
import com.bookheaven.user_service.service.UserService;
import com.bookheaven.user_service.service.WishlistService;
import com.bookheaven.user_service.util.UserResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;
    private final WishlistService wishlistService;
    private final ContactFormService contactFormService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(UserResponseBuilder.mapUserDto(userService.getUserById(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getSelfUser(Authentication authentication) {
        return ResponseEntity.ok(UserResponseBuilder.mapUserDto(userService.getSelfUser(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(UserResponseBuilder.mapUserDto(userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<String> changePassword(Authentication authentication,
                                                  @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request received");
        userService.changePassword(authentication, request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping("/me/profile-picture")
    public ResponseEntity<UserResponse> uploadProfilePicture(Authentication authentication,
                                                              @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Profile picture upload request received");
        return ResponseEntity.ok(UserResponseBuilder.mapUserDto(
                userService.updateProfilePicture(authentication, file)));
    }

    // ===================== ADDRESS (delegated to AddressService) =====================

    @PostMapping("/address")
    public ResponseEntity<UserResponse> addAddress(Authentication authentication, @RequestBody AddressRequest request) {
        return ResponseEntity.ok(UserResponseBuilder.mapUserDto(addressService.addAddress(authentication, request)));
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<UserResponse> updateAddress(Authentication authentication, @PathVariable Long addressId, @RequestBody AddressRequest request) {
        return ResponseEntity.ok(UserResponseBuilder.mapUserDto(addressService.updateAddress(authentication, addressId, request)));
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<UserResponse> deleteAddress(Authentication authentication, @PathVariable Long addressId) {
        return ResponseEntity.ok(UserResponseBuilder.mapUserDto(addressService.deleteAddress(authentication, addressId)));
    }

    // ===================== CONTACT (delegated to ContactFormService) =====================

    @PostMapping("/contact")
    public ResponseEntity<String> submitContactForm(@RequestBody ContactRequest request) {
        contactFormService.submitContactForm(request);
        return ResponseEntity.ok("Contact form submitted successfully.");
    }

    // ===================== WISHLIST (delegated to WishlistService) =====================

    @PostMapping("/wishlist/{bookId}")
    public ResponseEntity<Void> addWishlist(Authentication authentication, @PathVariable Long bookId) {
        wishlistService.addWishlist(authentication, bookId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/wishlist/{bookId}")
    public ResponseEntity<Void> removeWishlist(Authentication authentication, @PathVariable Long bookId) {
        wishlistService.removeWishlist(authentication, bookId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wishlist")
    public ResponseEntity<java.util.Set<Long>> getWishlistIds(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.getWishlistIds(authentication));
    }

    @GetMapping("/wishlist/details")
    public ResponseEntity<java.util.List<com.bookheaven.common.dto.response.BookPublicResponse>> getWishlistDetails(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.getWishlistDetails(authentication));
    }
}
