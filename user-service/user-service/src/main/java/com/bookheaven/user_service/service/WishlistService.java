package com.bookheaven.user_service.service;

import com.bookheaven.common.dto.response.BookPublicResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;

public interface WishlistService {
    void addWishlist(Authentication authentication, Long bookId);
    void removeWishlist(Authentication authentication, Long bookId);
    Set<Long> getWishlistIds(Authentication authentication);
    List<BookPublicResponse> getWishlistDetails(Authentication authentication);
}
