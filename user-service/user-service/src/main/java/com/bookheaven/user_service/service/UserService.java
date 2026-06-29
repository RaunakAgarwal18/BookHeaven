package com.bookheaven.user_service.service;

import com.bookheaven.user_service.dto.requestDto.ChangePasswordRequest;
import com.bookheaven.user_service.dto.requestDto.UpdateUserRequest;
import com.bookheaven.user_service.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface UserService {

    User getUserByEmail(String email);
    boolean userExistsByEmail(String email);
    boolean userExistsByUsername(String username);
    User saveUser(User user);
    User getUserById(UUID id);
    User updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
    User getSelfUser(Authentication authentication);
    void changePassword(Authentication authentication, ChangePasswordRequest request);
    void setPassword(Authentication authentication, com.bookheaven.user_service.dto.requestDto.ResetPasswordRequest request);
    User updateProfilePicture(Authentication authentication, MultipartFile file) throws IOException;
    User findByAuthProviderAndProviderId(User.AuthProvider provider, String providerId);
}
