package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.dto.requestDto.ChangePasswordRequest;
import com.bookheaven.user_service.dto.requestDto.UpdateUserRequest;
import com.bookheaven.user_service.entity.User;
import com.bookheaven.user_service.exception.PasswordMismatchException;
import com.bookheaven.user_service.exception.UserNotFoundException;
import com.bookheaven.user_service.repository.UserRepository;
import com.bookheaven.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserByEmail(String email) {
        log.info("Fetching user with email - {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public User getUserById(UUID id) {
        log.info("Fetching user with user ID - {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public User saveUser(User user) {
        log.info("Saving user with email id - {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with id - {}", id);
        User user = getUserById(id);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        if (request.getRazorpayAccountId() != null) {
            user.setRazorpayAccountId(request.getRazorpayAccountId());
        }
        log.info("Update for user - {} completed", id);
        return saveUser(user);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with id - {}", id);
        userRepository.delete(getUserById(id));
        log.info("User with id - {} deleted", id);
    }

    @Override
    public User getSelfUser(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return getUserById(userId);
    }

    @Override
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        User user = getSelfUser(authentication);
        log.info("Change password requested for user - {}", user.getEmail());

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Change password failed - incorrect old password for user {}", user.getEmail());
            throw new PasswordMismatchException("Incorrect current password.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        saveUser(user);
        log.info("Password changed successfully for user - {}", user.getEmail());
    }

    @Override
    public void setPassword(Authentication authentication, com.bookheaven.user_service.dto.requestDto.ResetPasswordRequest request) {
        User user = getSelfUser(authentication);
        log.info("Set password requested for user - {}", user.getEmail());

        if (!"NOT_SET".equals(user.getPassword()) && user.getPassword() != null) {
            throw new IllegalStateException("Password is already set. Use change password instead.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        saveUser(user);
        log.info("Password set successfully for user - {}", user.getEmail());
    }

    @Override
    public User updateProfilePicture(Authentication authentication, MultipartFile file) throws IOException {
        User user = getSelfUser(authentication);
        log.info("Profile picture upload requested for user - {}", user.getEmail());

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUri = "data:" + contentType + ";base64," + base64;

        user.setProfilePicture(dataUri);
        return saveUser(user);
    }

    @Override
    public User findByAuthProviderAndProviderId(User.AuthProvider provider, String providerId) {
        return userRepository.findByAuthProviderAndProviderId(provider, providerId).orElse(null);
    }
}
