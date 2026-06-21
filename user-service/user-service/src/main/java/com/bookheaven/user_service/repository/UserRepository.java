package com.bookheaven.user_service.repository;

import com.bookheaven.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> findByEmail(String email);
    public Optional<User> findByUsername(String userName);
    public boolean existsByEmail(String email);
    public boolean existsByUsername(String username);
    public Optional<User> findByAuthProviderAndProviderId(User.AuthProvider authProvider, String providerId);
}
